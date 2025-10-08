package org.example.model;

import java.io.Serializable;
public class ComplexReal implements Serializable {
    private final double a;
    private final double b;
    public ComplexReal(double a, double b) { this.a = a; this.b = b; }
    public double getA(){ return a; }
    public double getB(){ return b; }
    @Override public String toString(){ return String.format("%f+%fi", a, b); }
}