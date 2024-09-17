package kristianseng.skytrackvfr.utility;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

public class Symbol
{
    //region Fields

    private final Drawable _symbol;

    private final float _halfWidth, _halfHeight;

    //endregion

    //region Constructors

    public Symbol(Drawable symbol)
    {
        _symbol = symbol;

        _halfWidth = _symbol.getIntrinsicWidth()/2.0f;
        _halfHeight = _symbol.getIntrinsicHeight()/2.0f;
    }

    //endregion

    //region Public methods

    public void drawAtPosition(Canvas canv, float posX, float posY, float rotation, float scale)
    {
        _symbol.setBounds((int)(posX - _halfWidth), (int)(posY - _halfHeight), (int)(posX + _halfWidth), (int)(posY + _halfHeight));

        canv.save();
        canv.rotate(rotation, posX, posY);
        canv.scale(scale, scale, posX, posY);

        _symbol.draw(canv);

        canv.restore();
    }

    public PointF getDimensions()
    {
        return new PointF(_symbol.getIntrinsicWidth(), _symbol.getIntrinsicHeight());
    }

    //endregion
}
