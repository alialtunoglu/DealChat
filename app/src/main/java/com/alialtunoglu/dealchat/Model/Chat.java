package com.alialtunoglu.dealchat.Model;

public class Chat {
    String alici,gonderen,mesaj,resim,messageId;
    Boolean goruldu;
    String saat,tarih;

    public Chat(String alici, String gonderen, String mesaj, String resim, String messageId, Boolean goruldu, String saat, String tarih) {
        this.alici = alici;
        this.gonderen = gonderen;
        this.mesaj = mesaj;
        this.resim = resim;
        this.messageId = messageId;
        this.goruldu = goruldu;
        this.saat = saat;
        this.tarih = tarih;
    }

    public Chat() {
    }


    public String getAlici() {
        return alici;
    }

    public void setAlici(String alici) {
        this.alici = alici;
    }

    public String getGonderen() {
        return gonderen;
    }

    public void setGonderen(String gonderen) {
        this.gonderen = gonderen;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public String getResim() {
        return resim;
    }

    public void setResim(String resim) {
        this.resim = resim;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Boolean getGoruldu() {
        return goruldu;
    }

    public void setGoruldu(Boolean goruldu) {
        this.goruldu = goruldu;
    }

    public String getSaat() {
        return saat;
    }

    public void setSaat(String saat) {
        this.saat = saat;
    }

    public String getTarih() {
        return tarih;
    }

    public void setTarih(String tarih) {
        this.tarih = tarih;
    }
}
