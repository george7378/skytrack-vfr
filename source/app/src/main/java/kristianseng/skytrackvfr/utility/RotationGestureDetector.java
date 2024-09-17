package kristianseng.skytrackvfr.utility;

import android.view.MotionEvent;

public class RotationGestureDetector
{
    //region Interfaces

    public interface OnRotationGestureListener
    {
        void OnRotation(RotationGestureDetector rotationDetector);
    }

    //endregion

    //region Fields

    private float _fX, _fY, _sX, _sY;
    private int _ptrId1, _ptrId2;
    private float _angle, _angleDelta, _maxAngle;

    private OnRotationGestureListener _listener;

    private static final int INVALID_POINTER_ID = -1;

    //endregion

    //region Constructors

    public RotationGestureDetector(OnRotationGestureListener listener)
    {
        _listener = listener;

        _ptrId1 = INVALID_POINTER_ID;
        _ptrId2 = INVALID_POINTER_ID;
    }

    //endregion

    //region Public methods

    public float getMaxAngle()
    {
        return _maxAngle;
    }

    public float getAngleDelta()
    {
        return _angleDelta;
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                _ptrId1 = event.getPointerId(event.getActionIndex());
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                _ptrId2 = event.getPointerId(event.getActionIndex());
                _sX = event.getX(event.findPointerIndex(_ptrId1));
                _sY = event.getY(event.findPointerIndex(_ptrId1));
                _fX = event.getX(event.findPointerIndex(_ptrId2));
                _fY = event.getY(event.findPointerIndex(_ptrId2));
                break;

            case MotionEvent.ACTION_MOVE:
                if(_ptrId1 != INVALID_POINTER_ID && _ptrId2 != INVALID_POINTER_ID)
                {
                    float nsX = event.getX(event.findPointerIndex(_ptrId1));
                    float nsY = event.getY(event.findPointerIndex(_ptrId1));
                    float nfX = event.getX(event.findPointerIndex(_ptrId2));
                    float nfY = event.getY(event.findPointerIndex(_ptrId2));

                    float newAngle = angleBetweenLines(_fX, _fY, _sX, _sY, nfX, nfY, nsX, nsY);
                    _angleDelta = newAngle - _angle;
                    _angle = newAngle;

                    float angleAbsValue = Math.abs(_angle);
                    if (angleAbsValue > _maxAngle)
                        _maxAngle = angleAbsValue;

                    if (_listener != null)
                        _listener.OnRotation(this);
                }
                break;

            case MotionEvent.ACTION_UP:
                _ptrId1 = INVALID_POINTER_ID;
                _angle = 0;
                _maxAngle = 0;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                _ptrId2 = INVALID_POINTER_ID;
                _angle = 0;
                _maxAngle = 0;
                break;

            case MotionEvent.ACTION_CANCEL:
                _ptrId1 = INVALID_POINTER_ID;
                _ptrId2 = INVALID_POINTER_ID;
                _angle = 0;
                _maxAngle = 0;
                break;
        }

        return true;
    }

    //endregion

    //region Private methods

    private float angleBetweenLines(float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY)
    {
        float angle1 = (float)Math.atan2((fY - sY), (fX - sX));
        float angle2 = (float)Math.atan2((nfY - nsY), (nfX - nsX));

        float angle = ((float)Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f)
            angle += 360.0f;
        if (angle > 180.f)
            angle -= 360.0f;

        return angle;
    }

    //endregion
}