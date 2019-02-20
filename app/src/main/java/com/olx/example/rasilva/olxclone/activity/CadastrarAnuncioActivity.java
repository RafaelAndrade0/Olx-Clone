package com.olx.example.rasilva.olxclone.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.olx.example.rasilva.olxclone.R;
import com.olx.example.rasilva.olxclone.helper.ConfiguracaoFirebase;
import com.olx.example.rasilva.olxclone.helper.Permissoes;
import com.olx.example.rasilva.olxclone.model.Anuncio;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;


public class CadastrarAnuncioActivity extends AppCompatActivity
        implements View.OnClickListener, Validator.ValidationListener {

    private Button buttonSalvarAnuncio;

    @NotEmpty(message = "O campo é requirido")
    @Length(min = 5, max = 20, message = "O campo deve ter entre 5 e 20 caracteres")
    private EditText editTextTitulo;

    @NotEmpty(message = "O campo é requirido")
    @Length(min = 10, max = 100, message = "O campo deve ter entre 10 e 100 caracteres")
    private EditText editTextDescricao;

    @NotEmpty(message = "O campo é requirido")
    @Length(min = 10, message = "O telefone deve ter 10 caracteres")
    private EditText editTextTelefone;

    @Select
    private Spinner spinnerEstado, spinnerCategoria;

    private ImageView imagem1, imagem2, imagem3;

    private CurrencyEditText editTextValor;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @NotEmpty(message = "Selecione pelo menos uma imagem")
    // URI no dispostivo do usuario
    private List<String> listaFotosRecuperadas = new ArrayList<>();

    // Caminho das fotos no firebase
    private List<String> listaUrlFotos = new ArrayList<>();

    private Validator validator;
    private Anuncio anuncio;
    private StorageReference storage;
    private Picasso picasso;
    private android.app.AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        // Configurações Iniciais (Firebase)
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        // Seta Validações
        validator = new Validator(this);
        validator.setValidationListener(this);

        // Validar Permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        // Inicializa os componentes (Não diga)
        inicializarComponentes();

        // Carrega Dados Spinner
        carregarDadosSpinner();

        // Configuração para usar o cifrão de Reais(R$) - Para Testes
        editTextValor.configureViewForLocale(new Locale("PT", "BR"));

        // Listener do botão de Salvar Anuncio
        buttonSalvarAnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });
    }

    private void carregarDadosSpinner() {
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(this, R.array.categorias_array,
                android.R.layout.simple_spinner_item);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(this, R.array.estados_array,
                android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);
    }

    private void inicializarComponentes() {
        buttonSalvarAnuncio = findViewById(R.id.buttonSalvarAnuncio);
        editTextTitulo = findViewById(R.id.editTextTitulo);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        editTextValor = findViewById(R.id.editTextValor);
        editTextTelefone = findViewById(R.id.editTextPhone);

        // Configurando Spinners
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerEstado = findViewById(R.id.spinnerEstado);

        // Configurando Imagens
        imagem1 = findViewById(R.id.imageCadastroUm);
        imagem2 = findViewById(R.id.imageCadastroDois);
        imagem3 = findViewById(R.id.imageCadastroTres);

        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para usar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageCadastroUm:
                escolherImagem(1);
                break;
            case R.id.imageCadastroDois:
                escolherImagem(2);
                break;
            case R.id.imageCadastroTres:
                escolherImagem(3);
                break;
        }
    }

    public void escolherImagem(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            // Recupera a Imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();
            // Configura a imagem no imageView
            if (requestCode == 1) {
                Picasso.get()
                        .load(imagemSelecionada)
                        .centerCrop()
                        .resize(200, 100)
                        .into(imagem1);
//                imagem1.setImageURI(imagemSelecionada);
            } else if (requestCode == 2) {
                Picasso.get()
                        .load(imagemSelecionada)
                        .centerCrop()
                        .resize(200, 100)
                        .into(imagem2);
//                imagem2.setImageURI(imagemSelecionada);
            } else if (requestCode == 3) {
                Picasso.get()
                        .load(imagemSelecionada)
                        .centerCrop()
                        .resize(200, 100)
                        .into(imagem3);
//                imagem3.setImageURI(imagemSelecionada);
            }

            listaFotosRecuperadas.add(caminhoImagem);
        }
    }

    private Anuncio configurarAnuncio() {
        String estado = spinnerEstado.getSelectedItem().toString();
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String titulo = editTextTitulo.getText().toString();
        String valor = editTextValor.getText().toString();
        String telefone = editTextTelefone.getText().toString();
        String descricao = editTextDescricao.getText().toString();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;
    }

    // Todas verificações ocorreram bem
    @Override
    public void onValidationSucceeded() {

        // Mostra o dialog de Salvando Anuncio
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando anuncio....")
                .setCancelable(false)
                .build();
        alertDialog.show();

        anuncio = configurarAnuncio();

        if (!listaFotosRecuperadas.isEmpty()) {
            // Salvando as imagens
            for (int i = 0; i < listaFotosRecuperadas.size(); i++) {
                String urlImagem = listaFotosRecuperadas.get(i);
                int tamanhoLista = listaFotosRecuperadas.size();
                salvarFotoStorage(urlImagem, tamanhoLista, i);
            }
        } else {
            Toast.makeText(this, "Selecione pelo menos uma imagem!", Toast.LENGTH_SHORT).show();
        }
    }

    // Falhas nas verificações
    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else if (view instanceof Spinner) {
                ((TextView) ((Spinner) view).getSelectedView()).setError(message);
            }
        }
    }

    // Salva as fotos no firebase
    private void salvarFotoStorage(String urlString, final int tamanhoLista, int indice) {
        final StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem" + indice);
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlString));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Download de dados (url da Imagem)
                imagemAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri firebaseUrl = uri;
                        String urlConvertida = firebaseUrl.toString();
                        listaUrlFotos.add(urlConvertida);

                        // Se for igual o upload das imagens foi finalizado
                        if (tamanhoLista == listaUrlFotos.size()) {
                            // O Anuncio será salvo
                            anuncio.setFotos(listaUrlFotos);
                            // Salva o anuncio para a lista individual do usuario
                            anuncio.salvarAnuncio();
                            // Salva o anuncio para a lista publica
                            anuncio.salvarAnuncioPublico();

                            alertDialog.dismiss();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CadastrarAnuncioActivity.this, "Erro ao recuperar download url",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CadastrarAnuncioActivity.this, "Falha ao fazer o upload da imagem...",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

