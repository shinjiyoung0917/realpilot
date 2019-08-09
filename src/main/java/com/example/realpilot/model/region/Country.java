package com.example.realpilot.model.region;

import com.example.realpilot.utilAndConfig.CountryList;
import lombok.Data;

@Data
public class Country extends Regions {
    public void setCountry(String uid, CountryList countryList) {
        this.setUid(uid);
        this.setCountryName(countryList.getCountryName());
    }
}
