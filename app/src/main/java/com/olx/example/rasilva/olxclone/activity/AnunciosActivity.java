package com.olx.example.rasilva.olxclone.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.olx.example.rasilva.olxclone.R;
import com.olx.example.rasilva.olxclone.RecyclerItemClickListener;
import com.olx.example.rasilva.olxclone.adapter.AdapterMeusAnuncios;
import com.olx.example.rasilva.olxclone.helper.ConfiguracaoFirebase;
import com.olx.example.rasilva.olxclone.model.Anuncio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;


public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private List<Anuncio> meusAnuncios = new ArrayList<>();
    private AdapterMeusAnuncios adapterMeusAnuncios;
    private RecyclerView recyclerViewAnuncios;

    private Button buttonCategoria, buttonRegiao;

    private DatabaseReference anuncios;

    private android.app.AlertDialog alertDialog;

    String estadoSelecionadoString;
    private boolean estadoSelecionado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        inicializarComponentes();

        buttonRegiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarPorEstado();
            }
        });

        buttonCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarPorCategoria();
            }
        });

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Anuncios....")
                .setCancelable(false)
                .build();
        alertDialog.show();

        anuncios = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");
        recuperarAnunciosPublicos();

        // Configura Adapter
        adapterMeusAnuncios = new AdapterMeusAnuncios(meusAnuncios, this);

        // Configura o RecylerView
        recyclerViewAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnuncios.setHasFixedSize(true);
        recyclerViewAnuncios.setAdapter(adapterMeusAnuncios);

        // Click Listener da RecyclerView
        recyclerViewAnuncios.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
                recyclerViewAnuncios, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // Mandar objeto de usuario para a activty de details
                Anuncio anuncio = meusAnuncios.get(position);
                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                intent.putExtra("anuncio", anuncio);
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));
    }

    // Chamado apenas UMA vez para montar o menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Verificações no menu caso o usuario esteja logado (ou não)
    // Chamado antes do menu ser exibido (Chamado TODA vez)
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (autenticacao.getCurrentUser() == null) {
            menu.setGroupVisible(R.id.group_deslogado, true);
            menu.setGroupVisible(R.id.group_logado, false);
        } else {
            menu.setGroupVisible(R.id.group_deslogado, false);
            menu.setGroupVisible(R.id.group_logado, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // Seleção de Itens do menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cadastrar:
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
                break;

            case R.id.menu_anuncio:
                startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                break;

            case R.id.menu_sair:
                autenticacao.signOut();
                // Invalida o menu e faz executar o onPrepareOptionsMenu() novamente
                invalidateOptionsMenu();
//                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inicialização de todos componentes da Activity
    private void inicializarComponentes() {
        recyclerViewAnuncios = findViewById(R.id.recyclerViewAnuncios);
        buttonCategoria = findViewById(R.id.buttonCategoria);
        buttonRegiao = findViewById(R.id.buttonEstado);
    }

    // Recupera todos anuncios
    private void recuperarAnunciosPublicos() {
        anuncios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                meusAnuncios.clear();
                for (DataSnapshot estados : dataSnapshot.getChildren()) {
                    for (DataSnapshot categorias : estados.getChildren()) {
                        for (DataSnapshot anuncios : categorias.getChildren()) {
                            meusAnuncios.add(anuncios.getValue(Anuncio.class));
                        }
                    }
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

    // Dialog e Spinner do filtro de estado
    private void filtrarPorEstado() {
        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);

        // Configura Spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        dialogEstado.setView(viewSpinner);

        // Carrega dados do Spinner
        final Spinner spinner = viewSpinner.findViewById(R.id.spinnerFiltro);
        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(this, R.array.estados_array,
                android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterEstado);

        dialogEstado.setTitle("Selecione um estado");
        dialogEstado.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // O Filtro pelo estado ocorre aqui
                if (spinner.getSelectedItem().toString().equals("Estado")) {
                    Toast.makeText(AnunciosActivity.this, "Selecione um estado!",
                            Toast.LENGTH_SHORT).show();
                    estadoSelecionado = false;
                } else {
                    estadoSelecionadoString = spinner.getSelectedItem().toString();
                    anuncios = ConfiguracaoFirebase.getFirebase()
                            .child("anuncios")
                            .child(spinner.getSelectedItem().toString());
                    recuperarAnuncioFiltroEstado();
                    buttonRegiao.setText(estadoSelecionadoString);
                }
            }
        });
        dialogEstado.setNegativeButton("Nop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();
    }

    // Recupera anuncios de acordo com o filtro de estado
    private void recuperarAnuncioFiltroEstado() {
        anuncios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                meusAnuncios.clear();
                for (DataSnapshot categorias : dataSnapshot.getChildren()) {
                    for (DataSnapshot anuncios : categorias.getChildren()) {
                        meusAnuncios.add(anuncios.getValue(Anuncio.class));
                    }
                }
                Collections.reverse(meusAnuncios);
                adapterMeusAnuncios.notifyDataSetChanged();
                // Categoria estado já foi selecionada
                estadoSelecionado = true;
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void filtrarPorCategoria() {
        AlertDialog.Builder dialogCategoria = new AlertDialog.Builder(this);

        // Configura Spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        dialogCategoria.setView(viewSpinner);

        // Carrega dados do Spinner
        final Spinner spinner = viewSpinner.findViewById(R.id.spinnerFiltro);
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(this, R.array.categorias_array,
                android.R.layout.simple_spinner_item);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterCategoria);

        dialogCategoria.setTitle("Selecione uma Categoria");
        dialogCategoria.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (estadoSelecionado) {
                    anuncios = ConfiguracaoFirebase.getFirebase()
                            .child("anuncios")
                            .child(estadoSelecionadoString)
                            .child(spinner.getSelectedItem().toString());
                    recuperarAnuncioFiltroCategoria();

                    // Muda Texto do botão para refletir a escolha atual
                    buttonCategoria.setText(spinner.getSelectedItem().toString());
                } else {
                    Toast.makeText(AnunciosActivity.this, "Selecione um estado primeiro",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogCategoria.setNegativeButton("Nop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = dialogCategoria.create();
        dialog.show();
    }

    private void recuperarAnuncioFiltroCategoria() {
        anuncios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                meusAnuncios.clear();
                for (DataSnapshot anuncios : dataSnapshot.getChildren()) {
                    meusAnuncios.add(anuncios.getValue(Anuncio.class));
                }

                Collections.reverse(meusAnuncios);
                adapterMeusAnuncios.notifyDataSetChanged();
                // Depois do resultado fica como false
//                estadoSelecionado = false;
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
