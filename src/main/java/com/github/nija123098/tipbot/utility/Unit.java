package com.github.nija123098.tipbot.utility;

import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public enum Unit {
    BEER(() -> dollarToDash(2.99F)),
    COFFEE(() -> dollarToDash(3.99F)),
    COOKIE(() -> dollarToDash(.50F)),
    TOOTHPASTE(() -> dollarToDash(1.99F)),
    DASH(() -> 1D),
    $(() -> dollarToDash(1)),
    USD(() -> dollarToDash(1)),
    EUR(() -> getUnitToDash("EUR")),
    GBP(() -> getUnitToDash("GBP")),
    MXN(() -> getUnitToDash("MXN")),;
    Unit(ToDashAmount dashAmount){
        this.dashAmount = dashAmount;
    }
    private ToDashAmount dashAmount;
    public Double getDashAmount(){
        return dashAmount.getAmount();
    }
    private interface ToDashAmount{
        Double getAmount();
    }
    private static double dollarToDash(float amount){
        return amount / getDashValue("USD");
    }
    public static double getUnitToDash(String currency){
        return 1 / getDashValue(currency);
    }
    private static float getDashValue(String currency) {
        try {
            return Float.parseFloat(String.valueOf(new JsonParser().parse(Unirest.get("https://min-api.cryptocompare.com/data/price?fsym=DASH&tsyms=" + currency).asString().getBody()).getAsJsonObject().get(currency)));
        } catch (UnirestException e){
            throw new WrappingException(e);
        }
    }
}
