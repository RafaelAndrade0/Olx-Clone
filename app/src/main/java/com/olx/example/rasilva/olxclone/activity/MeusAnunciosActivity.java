package com.olx.example.rasilva.olxclone.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.olx.example.rasilva.olxclone.R;
import com.olx.example.rasilva.olxclone.adapter.AdapterMeusAnuncios;
import com.olx.example.rasilva.olxclone.helper.ConfiguracaoFirebase;
import com.olx.example.rasilva.olxclone.model.Anuncio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MeusAnunciosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAnuncios;
    private List<Anuncio> meusAnuncios = new ArrayList<>();
    private DatabaseReference anuncios;
    private AdapterMeusAnuncios adapterMeusAnuncios;
    private android.app.AlertDialog alertDialog;
    private DatabaseReference firebase = ConfiguracaoFirebase.getFirebase();
    private DatabaseReference anunciosPublicos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CadastrarAnuncioActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Exibe alert dialog de carregando
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Anuncios....")
                .setCancelable(false)
                .build();
        alertDialog.show();

        // Configurações Iniciais
        anuncios = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios")
                .child( ConfiguracaoFirebase.getIdUsuario() );

        // Inicializa os Componentes
        this.inicializarComponentes();

        // Configura o swipe
        swipe();

        // Configura Adapter
        adapterMeusAnuncios = new AdapterMeusAnuncios(meusAnuncios, this);

        // Configura o RecylerView
        recyclerViewAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnuncios.setHasFixedSize(true);
        recyclerViewAnuncios.setAdapter(adapterMeusAnuncios);

        this.recuperarAnuncios();

    }

    private void inicializarComponentes() {
        recyclerViewAnuncios = findViewById(R.id.recyclerViewAnuncios);
    }

    private void recuperarAnuncios() {
        anuncios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                meusAnuncios.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    meusAnuncios.add(ds.getValue(Anuncio.class));
                }

                Collections.reverse(meusAnuncios);
                adapterMeusAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void swipe() {
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags | ItemTouchHelper.START);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                excluirAnuncio(viewHolder);
            }
        };
        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerViewAnuncios);
    }

    private void excluirAnuncio(final RecyclerView.ViewHolder viewHolder) {
        AlertDialog.Builder alertDialogo = new AlertDialog.Builder(this);
        alertDialogo.setTitle("Excluir o anuncio?");
        alertDialogo.setMessage("Tem certeza que deseja excluir este anuncio?");
        alertDialogo.setCancelable(false);
        alertDialogo.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition();
                Anuncio anuncio = meusAnuncios.get(position);

                // Retorna a referencia em meus anuncios
                anuncios = firebase.child("meus_anuncios")
                        .child(ConfiguracaoFirebase.getIdUsuario())
                        .child(anuncio.getIdAnuncio());

                // Retorna a referencia nos anuncios publicos
                anunciosPublicos = firebase.child("anuncios")
                        .child(anuncio.getEstado())
                        .child(anuncio.getCategoria())
                        .child(anuncio.getIdAnuncio());

                // Remove a chave retornada de meus anuncios
                anuncios.removeValue();

                // Remove a chave retornada dos anuncios publicos
                anunciosPublicos.removeValue();

                // Notifica Adapter
                adapterMeusAnuncios.notifyItemRemoved(position);
            }
        });
        alertDialogo.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MeusAnunciosActivity.this, "Cancelado!",
                        Toast.LENGTH_SHORT).show();
                adapterMeusAnuncios.notifyDataSetChanged();
            }
        });
        alertDialogo.show();
    }
}
