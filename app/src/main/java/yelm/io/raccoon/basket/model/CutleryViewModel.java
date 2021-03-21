package yelm.io.raccoon.basket.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigDecimal;

public class CutleryViewModel extends ViewModel {

    private static MutableLiveData<BigDecimal> cutleryCount;

    public MutableLiveData<BigDecimal> getCashBack() {
        if (cutleryCount == null) {
            cutleryCount = new MutableLiveData<>();
            cutleryCount.setValue(new BigDecimal("1"));
        }
        return cutleryCount;
    }
}
