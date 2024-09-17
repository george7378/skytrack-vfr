package kristianseng.skytrackvfr.utility;

import java.util.Observable;

public class ObservableValue<T> extends Observable
{
    //region Fields

    private T _value;

    //endregion

    //region Constructors

    public ObservableValue()
    {

    }

    public ObservableValue(T initialValue)
    {
        _value = initialValue;
    }

    //endregion

    //region Public methods

    public T getValue()
    {
        return _value;
    }

    public void setValue(T newValue)
    {
        _value = newValue;

        setChanged();
        notifyObservers();
    }

    //endregion
}