package com.olx.example.rasilva.olxclone.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;
import com.olx.example.rasilva.olxclone.helper.ConfiguracaoFirebase;

import java.util.List;

public class Anuncio implements Parcelable {
    private String idAnuncio;
    private String estado;
    private String categoria;
    private String titulo;
    private String valor;
    private String telefone;
    private String descricao;
    private List<String> fotos;

    public Anuncio() {
        // Seta o id do anuncio
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios");
        setIdAnuncio(anuncioRef.push().getKey());
    }

    protected Anuncio(Parcel in) {
        idAnuncio = in.readString();
        estado = in.readString();
        categoria = in.readString();
        titulo = in.readString();
        valor = in.readString();
        telefone = in.readString();
        descricao = in.readString();
        fotos = in.createStringArrayList();
    }

    public static final Creator<Anuncio> CREATOR = new Creator<Anuncio>() {
        @Override
        public Anuncio createFromParcel(Parcel in) {
            return new Anuncio(in);
        }

        @Override
        public Anuncio[] newArray(int size) {
            return new Anuncio[size];
        }
    };

    public void salvarAnuncio() {
        String idUsuario = ConfiguracaoFirebase.getIdUsuario();
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios");
        anuncioRef.child(idUsuario)
                .child(getIdAnuncio())
                .setValue(this);
    }

    public void salvarAnuncioPublico() {
        String idUsuario = ConfiguracaoFirebase.getIdUsuario();
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");
                anuncioRef.child(getEstado())
                        .child(getCategoria())
                        .child(getIdAnuncio())
                .setValue(this);
    }

    public void salvarAnuncioFavorito() {
        String idUsuario = ConfiguracaoFirebase.getIdUsuario();
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_favoritos");
        anuncioRef.child(idUsuario)
                .child(getIdAnuncio())
                .setValue(this);
    }

    public Anuncio(String idAnuncio, String estado, String categoria,
                   String titulo, String valor, String telefone, String descricao, List<String> fotos) {
        this.idAnuncio = idAnuncio;
        this.estado = estado;
        this.categoria = categoria;
        this.titulo = titulo;
        this.valor = valor;
        this.telefone = telefone;
        this.descricao = descricao;
        this.fotos = fotos;
    }

    public String getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(String idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idAnuncio);
        dest.writeString(estado);
        dest.writeString(categoria);
        dest.writeString(titulo);
        dest.writeString(valor);
        dest.writeString(telefone);
        dest.writeString(descricao);
        dest.writeStringList(fotos);
    }
}
