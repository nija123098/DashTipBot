package com.github.nija123098.tipbot.utility;

import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

public enum Unit {
    BEER(() -> dollarToDash(3.99F)),
    COFFEE(() -> dollarToDash(2.99F)),
    TEA(() -> dollarToDash(2.99F)),
    COOKIE(() -> dollarToDash(.50F)),
    TOOTHPASTE(() -> dollarToDash(1.99F)),
    DASH(() -> 1D),
    USD(() -> dollarToDash(1), "$"),
    EUR(() -> getUnitToDash("EUR"), "€"),
    GBP(() -> getUnitToDash("GBP"), "£"),
    RUB(() -> getUnitToDash("RUB"), (amount) -> amount + "₽", "₽", "ruble"),
    CAD(() -> getUnitToDash("CAD"), "C$", "Can$"),
    SGD(() -> getUnitToDash("SGD"), "S$"),
    JPY(() -> getUnitToDash("JPY")),
    AUD(() -> getUnitToDash("AUD"), "A$", "AU$"),
    RON(() -> getUnitToDash("RON")),
    CNY(() -> getUnitToDash("CNY")),
    CZK(() -> getUnitToDash("CZK"), "Kč"),
    CHF(() -> getUnitToDash("CHF")),
    BGN(() -> getUnitToDash("BGN")),
    PLN(() -> getUnitToDash("PLN"), "zł", "zloty", "zlotys"),
    MYR(() -> getUnitToDash("MYR")),
    ZAR(() -> getUnitToDash("ZAR")),// 14.46
    SEK(() -> getUnitToDash("SEL"), "kr"),
    INR(() -> getUnitToDash("INR"), "₹"),
    HKD(() -> getUnitToDash("HKD"), "HK$"),
    BRL(() -> getUnitToDash("BRL"), "R$"),
    PKR(() -> getUnitToDash("PKR")),
    MXN(() -> getUnitToDash("MXN"), "Mex$"),;

    Unit(ToDashAmount dashAmount, String... names) {
        this(dashAmount, 2, names);
    }

    Unit(ToDashAmount dashAmount, int decimals, String... names) {
        this(dashAmount, null, names);
        this.display = (amount) -> {
            String decimal = Unit.displayAmount(amount, decimals);
            return this.names.size() > 1 ? this.names.get(1) + decimal : (decimal + " " + this.names.get(0));
        };
    }

    Unit(ToDashAmount dashAmount, Function<Double, String> display, String... names) {
        this.dashAmount = dashAmount;
        this.display = display;
        this.names.add(this.name());
        Collections.addAll(this.names, names);
    }

    private final ToDashAmount dashAmount;
    private Function<Double, String> display;
    private final List<String> names = new ArrayList<>(1);

    public String display(double amount) {
        return this.display.apply(amount);
    }

    public Double getDashAmount() {
        return this.dashAmount.getAmount();
    }

    private interface ToDashAmount {
        Double getAmount();
    }

    private static double dollarToDash(float amount) {
        return amount / getDashValue("USD");
    }

    public static double getUnitToDash(String currency) {
        return 1 / getDashValue(currency);
    }

    private static final AtomicReference<Float> DASH_VALUE = new AtomicReference<>();
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private static float getDashValue(String currency) {
        if (DASH_VALUE.get() == null) {
            try {
                EXECUTOR_SERVICE.schedule(() -> DASH_VALUE.set(null), 1, TimeUnit.MINUTES);
                DASH_VALUE.set(Float.parseFloat(String.valueOf(new JsonParser().parse(Unirest.get("https://min-api.cryptocompare.com/data/price?fsym=DASH&tsyms=" + currency).asString().getBody()).getAsJsonObject().get(currency))));
            } catch (UnirestException e) {
                throw new WrappingException(e);
            }
        }
        return DASH_VALUE.get();
    }

    private static final Set<String> NAMES = new HashSet<>();

    static {
        Stream.of(Unit.values()).forEach(unit -> NAMES.addAll(unit.names));
    }

    public static Set<String> getNames() {
        return NAMES;
    }

    private static final Map<String, Unit> NAME_MAP = new HashMap<>();

    static {
        Stream.of(Unit.values()).forEach(unit -> unit.names.forEach(name -> NAME_MAP.put(name.toLowerCase(), unit)));
    }

    public static Unit getUnitForName(String name) {
        return NAME_MAP.get(name.toLowerCase());
    }

    public static String displayAmount(double amount, int decimals) {
        String s = String.valueOf(amount);
        int index = s.indexOf("."), targetLength = index + decimals;
        if (decimals < 1) return s.substring(0, index);
        StringBuilder builder = new StringBuilder(s.substring(0, Math.min(s.length(), targetLength + 1)));
        for (int i = s.length() - 1; i < targetLength; i++) builder.append("0");
        return builder.toString();
    }
}
