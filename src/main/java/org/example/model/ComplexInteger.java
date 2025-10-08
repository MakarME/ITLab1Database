package org.example.model;

import java.io.Serializable;
public class ComplexInteger implements Serializable {
    private final long a;
    private final long b;

    public ComplexInteger(long a, long b) { this.a = a; this.b = b; }
    public long getA() { return a; }
    public long getB() { return b; }
    @Override public String toString(){ return String.format("%d+%di", a, b); }
}