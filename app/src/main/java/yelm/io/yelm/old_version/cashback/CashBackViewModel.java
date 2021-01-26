package yelm.io.yelm.old_version.cashback;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigDecimal;

public class CashBackViewModel extends ViewModel {

    private static MutableLiveData<BigDecimal> cashBack;

    public MutableLiveData<BigDecimal> getCashBack() {
        if (cashBack == null) {
            cashBack = new MutableLiveData<>();
            cashBack.setValue(new BigDecimal("0"));
        }
        return cashBack;
    }
}
