package com.nugsky.tugasakhir.utils;

public interface ResultType {
    ResultType UNTAMPERED = ResultType.UNTAMPERED.UNTAMPERED;
    enum TAMPERED{
        FRAMEDELETION
    }
    enum UNTAMPERED{
        UNTAMPERED
    }
}
