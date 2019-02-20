package com.olx.example.rasilva.olxclone.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olx.example.rasilva.olxclone.R;
import com.olx.example.rasilva.olxclone.model.Anuncio;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterMeusAnuncios extends RecyclerView.Adapter<AdapterMeusAnuncios.MyViewHolder> {

    private List<Anuncio> meusAnuncios;
    private Picasso picasso;
    private Context context;

    public AdapterMeusAnuncios(List<Anuncio> meusAnuncios, Context context) {
        this.meusAnuncios = meusAnuncios;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLista = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_meusanuncios, viewGroup, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Anuncio anuncio = meusAnuncios.get(i);
        myViewHolder.textViewTitulo.setText(anuncio.getTitulo());
        myViewHolder.textViewValor.setText(anuncio.getValor());
        Picasso.get()
                .load(anuncio.getFotos().get(0))
                .resize(100, 100)
                .centerCrop()
                .placeholder(R.drawable.placeholder3)
                .into(myViewHolder.imageViewAnuncio);
    }

    @Override
    public int getItemCount() {
        return meusAnuncios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitulo;
        TextView textViewValor;
        ImageView imageViewAnuncio;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitulo = itemView.findViewById(R.id.textViewTitulo);
            textViewValor = itemView.findViewById(R.id.textViewValor);
            imageViewAnuncio = itemView.findViewById(R.id.imageViewAnuncio);
        }
    }
}
