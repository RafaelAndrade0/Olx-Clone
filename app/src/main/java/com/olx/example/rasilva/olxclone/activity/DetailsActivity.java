package com.olx.example.rasilva.olxclone.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.olx.example.rasilva.olxclone.R;
import com.olx.example.rasilva.olxclone.helper.ConfiguracaoFirebase;
import com.olx.example.rasilva.olxclone.model.Anuncio;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private Anuncio anuncio;
    private CarouselView carouselView;
    private TextView textTitulo, textValor, textDescricao;
    private Button buttonLigacao;
    private Menu menu;
    private boolean controleIconeFavorito = true;

    private DatabaseReference anuncios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        inicializaComponentes();

        // Recebendo o parcelable da activty de Anuncios
        anuncio = getIntent().getParcelableExtra("anuncio");
        final List<String> fotosAnuncios = anuncio.getFotos();

        // Recuperar caso o favorito já tenha sido adicionado
        anuncios = ConfiguracaoFirebase.getFirebase()
                .child("meus_favoritos")
                .child(ConfiguracaoFirebase.getIdUsuario());

        // Controle do icone (Se o anuncio já é favorito ou não)
//        mudaIconeFavorito();

        carouselView.setPageCount(fotosAnuncios.size());
        carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                Picasso.get()
                        .load(fotosAnuncios.get(position))
                        .into(imageView);
            }
        });

        textDescricao.setText(anuncio.getDescricao());
        textValor.setText(anuncio.getValor());
        textTitulo.setText(anuncio.getTitulo());

        // Botão para abrir o dial do telefone
        buttonLigacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numeroTelefone = anuncio.getTelefone();
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                        "tel", numeroTelefone, null));
                startActivity(phoneIntent);
            }
        });

//        menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_red_24dp));
    }

    private void inicializaComponentes() {
        carouselView = findViewById(R.id.carouselView);
        textTitulo = findViewById(R.id.textTitulo);
        textValor = findViewById(R.id.textValor);
        textDescricao = findViewById(R.id.textDescricao);
        buttonLigacao = findViewById(R.id.buttonLigacao);
    }

    // Chamado uma unica vez para montar o menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
//        if (!controleIconeFavorito) {
//            menu.findItem(R.id.like_button).setIcon(R.drawable.ic_favorite_red_24dp);
//        } else {
//            menu.findItem(R.id.like_button).setIcon(R.drawable.ic_favorite_white_24dp);
//        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.like_button:
//                if (controleIconeFavorito) {
//                    item.setIcon(R.drawable.ic_favorite_red_24dp);
//                } else {
//                    item.setIcon(R.drawable.ic_favorite_white_24dp);
//                }
                existenciaAnuncioFavorito();
                controleIconeFavorito = !controleIconeFavorito;
//                invalidateOptionsMenu();
                break;
            case R.id.share_button:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = anuncio.getTitulo();
//                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Compartilhar Com..."));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void mudaIconeFavorito() {
        anuncios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String dataKey = data.getKey();
                    String anuncioKey = anuncio.getIdAnuncio();
                    if (dataKey.equals(anuncioKey)) {
                        controleIconeFavorito = false;
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Verifica se o anuncio já existe na lista de meus favoritos e o exclui dela
    private void existenciaAnuncioFavorito() {
        anuncios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String dataKey = data.getKey();
                    String anuncioKey = anuncio.getIdAnuncio();
                    if (dataKey.equals(anuncioKey)) {
                        Toast.makeText(DetailsActivity.this, "Já Existe",
                                Toast.LENGTH_LONG).show();
                        anuncios.child(dataKey).removeValue();
                        controleIconeFavorito = false;
                        return;
                    } else {
                        anuncio.salvarAnuncioFavorito();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
