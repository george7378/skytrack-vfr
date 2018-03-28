package kristianseng.skytrackvfr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import kristianseng.skytrackvfr.utility.Coords;
import kristianseng.skytrackvfr.utility.Global;
import kristianseng.skytrackvfr.utility.RotationGestureDetector;
import kristianseng.skytrackvfr.utility.RouteLeg;
import kristianseng.skytrackvfr.utility.Symbol;
import kristianseng.skytrackvfr.utility.Waypoint;

public class NavDisplayView extends View implements RotationGestureDetector.OnRotationGestureListener
{
    //region Private classes

    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            Global.MapCentre.x -= (distanceY * Math.cos(Math.toRadians(Global.MapRotation.getValue())) + distanceX * Math.sin(Math.toRadians(Global.MapRotation.getValue())))/Global.MapScaleFactor;
            Global.MapCentre.y += (distanceX * Math.cos(Math.toRadians(Global.MapRotation.getValue())) - distanceY * Math.sin(Math.toRadians(Global.MapRotation.getValue())))/Global.MapScaleFactor;

            invalidate();
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            Global.MapScaleFactor *= detector.getScaleFactor();

            invalidate();
            return true;
        }
    }

    //endregion

    //region Members

    public boolean ShowLabels;

    //endregion

    //region Fields

    private final GestureDetector _gestureDetector;
    private final ScaleGestureDetector _scaleGestureDetector;
    private final RotationGestureDetector _rotationGestureDetector;

    private final Paint _paintGridLines = new Paint(), _paintRoute = new Paint(), _paintLabels = new Paint(), _paintLabelBackground = new Paint();
    private final Symbol _waypointSymbol, _terminalWaypointSymbol, _aircraftSymbol, _aircraftSymbolNoHeading;

    private float _canvasCentreX, _canvasCentreY;
    private final int _labelPadding;
    private final Rect _labelBounds = new Rect();
    private final RectF _labelBackgroundRect = new RectF();
    private final PointF _waypointSymbolSize;

    //endregion

    //region Constructors

    public NavDisplayView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        _gestureDetector = new GestureDetector(context, new GestureListener());
        _scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        _rotationGestureDetector = new RotationGestureDetector(this);

        _paintGridLines.setStrokeWidth(1);
        _paintGridLines.setColor(Color.GRAY);
        _paintGridLines.setPathEffect(new DashPathEffect(new float[] {10, 20}, 0));

        _paintRoute.setStrokeWidth(2);
        _paintRoute.setColor(Color.BLACK);
        _paintRoute.setAntiAlias(true);

        _paintLabels.setTextSize(15*getResources().getDisplayMetrics().scaledDensity);
        _paintLabels.setColor(Color.BLACK);
        _paintLabels.setAntiAlias(true);

        _paintLabelBackground.setColor(ContextCompat.getColor(context, R.color.colorNavDisplayLabelBackground));
        _paintLabelBackground.setAntiAlias(true);

        _waypointSymbol = new Symbol(ContextCompat.getDrawable(context, R.mipmap.ic_waypoint));
        _terminalWaypointSymbol = new Symbol(ContextCompat.getDrawable(context, R.mipmap.ic_terminal_waypoint));
        _aircraftSymbol = new Symbol(ContextCompat.getDrawable(context, R.mipmap.ic_aircraft_location));
        _aircraftSymbolNoHeading = new Symbol(ContextCompat.getDrawable(context, R.mipmap.ic_aircraft_location_noheading));

        _labelPadding = (int)(5*getResources().getDisplayMetrics().scaledDensity);
        _waypointSymbolSize = _waypointSymbol.getDimensions();
    }

    //endregion

    //region Overrides

    @Override
    protected void onDraw(Canvas canvas)
    {
        _canvasCentreX = canvas.getWidth()/2.0f;
        _canvasCentreY = canvas.getHeight()/2.0f;

        // Grid lines
        int step = Global.MapScaleFactor < 50 ? 10 : 1;
        for (int lat = -90; lat <= 90; lat += step)
        {
            PointF start = coordsToPixels(lat, -180), end = coordsToPixels(lat, 180);
            canvas.drawLine(start.x, start.y, end.x, end.y, _paintGridLines);
        }
        for (int lon = -180; lon <= 180; lon += step)
        {
            PointF start = coordsToPixels(-90, lon), end = coordsToPixels(90, lon);
            canvas.drawLine(start.x, start.y, end.x, end.y, _paintGridLines);
        }

        // Route
        if (Global.ActiveRoute != null)
        {
            for (RouteLeg leg : Global.ActiveRoute.Legs)
            {
                for (int i = 0; i < leg.GreatCirclePoints.size() - 1; i++)
                {
                    PointF start = coordsToPixels(leg.GreatCirclePoints.get(i)), end = coordsToPixels(leg.GreatCirclePoints.get(i + 1));
                    canvas.drawLine(start.x, start.y, end.x, end.y, _paintRoute);

                    // Waypoint symbols
                    if (i == 0)
                    {
                        if (Global.ActiveRoute.Legs.indexOf(leg) == 0)
                        {
                            _terminalWaypointSymbol.drawAtPosition(canvas, start.x, start.y, -Global.MapRotation.getValue(), Global.SymbolScale);
                        }
                        else
                        {
                            _waypointSymbol.drawAtPosition(canvas, start.x, start.y, -Global.MapRotation.getValue(), Global.SymbolScale);
                        }
                    }
                    if (Global.ActiveRoute.Legs.indexOf(leg) == Global.ActiveRoute.Legs.size() - 1 && i == leg.GreatCirclePoints.size() - 2)
                    {
                        _terminalWaypointSymbol.drawAtPosition(canvas, end.x, end.y, -Global.MapRotation.getValue(), Global.SymbolScale);
                    }
                }
            }
        }

        // Aircraft
        Coords aircraftLocation = Global.LastAircraftLocation.getValue();
        if (aircraftLocation != null)
        {
            PointF aircraftLocationPixels = coordsToPixels(aircraftLocation);

            if (Global.LastAircraftHeading != null)
            {
                _aircraftSymbol.drawAtPosition(canvas, aircraftLocationPixels.x, aircraftLocationPixels.y, Global.LastAircraftHeading - Global.MapRotation.getValue(), Global.SymbolScale);
            }
            else
                _aircraftSymbolNoHeading.drawAtPosition(canvas, aircraftLocationPixels.x, aircraftLocationPixels.y, -Global.MapRotation.getValue(), Global.SymbolScale);
        }

        // Labels
        if (ShowLabels && Global.ActiveRoute != null)
        {
            for (RouteLeg leg : Global.ActiveRoute.Legs)
            {
                PointF startLabelPos = coordsToPixels(leg.StartPoint.Coords);
                startLabelPos.x += _waypointSymbolSize.x*Global.SymbolScale/2;
                startLabelPos.y -= _waypointSymbolSize.y*Global.SymbolScale/2;

                String startLabel = getLabel(leg.StartPoint);
                _paintLabels.getTextBounds(startLabel, 0, startLabel.length(), _labelBounds);
                updateLabelBackgroundBounds(startLabelPos, _labelPadding);

                canvas.drawRoundRect(_labelBackgroundRect, _labelPadding, _labelPadding, _paintLabelBackground);
                canvas.drawText(startLabel, startLabelPos.x, startLabelPos.y, _paintLabels);

                if (Global.ActiveRoute.Legs.indexOf(leg) == Global.ActiveRoute.Legs.size() - 1)
                {
                    PointF endLabelPos = coordsToPixels(leg.EndPoint.Coords);
                    endLabelPos.x += _waypointSymbolSize.x*Global.SymbolScale/2;
                    endLabelPos.y -= _waypointSymbolSize.y*Global.SymbolScale/2;

                    String endLabel = getLabel(leg.EndPoint);
                    _paintLabels.getTextBounds(endLabel, 0, endLabel.length(), _labelBounds);
                    updateLabelBackgroundBounds(endLabelPos, _labelPadding);

                    canvas.drawRoundRect(_labelBackgroundRect, _labelPadding, _labelPadding, _paintLabelBackground);
                    canvas.drawText(endLabel, endLabelPos.x, endLabelPos.y, _paintLabels);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!Global.CentreOnAircraft.getValue())
            _gestureDetector.onTouchEvent(event);

        _scaleGestureDetector.onTouchEvent(event);

        if (!Global.TrackUp.getValue())
            _rotationGestureDetector.onTouchEvent(event);

        return true;
    }

    //endregion

    //region Private methods

    private PointF coordsToPixels(double lat, double lon)
    {
        double centreToPointLatPixels = (lat - Global.MapCentre.x)*Global.MapScaleFactor;
        double centreToPointLonPixels = (lon - Global.MapCentre.y)*Global.MapScaleFactor;

        double centreToPointRotatedLatPixels = centreToPointLonPixels*Math.sin(Math.toRadians(Global.MapRotation.getValue())) + centreToPointLatPixels*Math.cos(Math.toRadians(Global.MapRotation.getValue()));
        double centreToPointRotatedLonPixels = centreToPointLonPixels*Math.cos(Math.toRadians(Global.MapRotation.getValue())) - centreToPointLatPixels*Math.sin(Math.toRadians(Global.MapRotation.getValue()));

        return new PointF((float)(_canvasCentreX + centreToPointRotatedLonPixels), (float)(_canvasCentreY - centreToPointRotatedLatPixels));
    }

    private PointF coordsToPixels(Coords c)
    {
        return coordsToPixels(c.Latitude, c.Longitude);
    }

    private String getLabel(Waypoint wp)
    {
        return wp.Name;
    }

    private void updateLabelBackgroundBounds(PointF labelPos, int padding)
    {
        _labelBackgroundRect.left = labelPos.x + _labelBounds.left - padding;
        _labelBackgroundRect.top = labelPos.y + _labelBounds.top - padding;
        _labelBackgroundRect.right = _labelBackgroundRect.left + _labelBounds.width() + 2*padding;
        _labelBackgroundRect.bottom = _labelBackgroundRect.top + _labelBounds.height() + 2*padding;
    }

    //endregion

    //region OnRotationGestureListener implementation

    @Override
    public void OnRotation(RotationGestureDetector rotationDetector)
    {
        if (rotationDetector.getMaxAngle() > 10)
            Global.MapRotation.setValue(Global.MapRotation.getValue() + rotationDetector.getAngleDelta());
    }

    //endregion
}