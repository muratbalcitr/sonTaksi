package com.murat.murat.sontaxi;

/**
 * Created by murat on 04.04.2017.
 */

public class Taksiduraklari {
    public String durakAdi;
    public String durakNumarasi;
    public String iller;

    public Taksiduraklari() {
    }

    public Taksiduraklari(String durakadi, String duraknumarasi) {
        this.durakAdi = durakadi;
        this.durakNumarasi = duraknumarasi;
    }

    public String getDurakAdi() {
        return durakAdi;
    }

    public void setDurakAdi(String durakAdi) {
        this.durakAdi = durakAdi;
    }

    public String getDurakNumarasi() {
        return durakNumarasi;
    }

    public void setDurakNumarasi(String durakNumarasi) {
        this.durakNumarasi = durakNumarasi;
    }

    public void setIller(String iller) {
        this.iller = iller;
    }

    public String getIller() {
        return iller;
    }
}
