package com.example.realpilot.model.region;

import com.example.realpilot.utilAndConfig.CountryList;
import lombok.Data;

@Data
public class Country extends Regions {
    public void setCountry(Country country, CountryList countryList) {
        this.setUid(country.getUid());
        this.setCountryName(countryList.getCountryName());
        /*this.uid = country.getUid();
        this.countryName = countryList.getCountryName();*/
    }
}
