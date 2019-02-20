package com.olx.example.rasilva.olxclone.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.olx.example.rasilva.olxclone.R;
import com.olx.example.rasilva.olxclone.helper.ConfiguracaoFirebase;
import com.olx.example.rasilva.olxclone.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if (!email.isEmpty()) {
                    if (!senha.isEmpty()) {
                        // Verifica o estado do switch
                        if (tipoAcesso.isChecked()) {
                            // Cadastro
                            realizaCadastro(email,senha);
                        } else {
                            // Login
                            realizaLogin(email, senha);
                        }
                    } else {
                        Toast.makeText(CadastroActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastroActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void inicializarComponentes() {
        botaoAcessar = findViewById(R.id.buttonAcesso);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        tipoAcesso = findViewById(R.id.switchAcesso);
    }

    public void realizaLogin(String email, String senha) {
        autenticacao.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CadastroActivity.this,
                                    "Logado com Sucesso", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
                            finish();
                        } else {
                            Toast.makeText(CadastroActivity.this,
                                    task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void realizaCadastro(String email, String senha) {
        autenticacao.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CadastroActivity.this,
                                    "Cadastro Realizado com Sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            String erroExecacao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                erroExecacao = "Password fraco!";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                erroExecacao = "Digite um e-mail valido";
                            } catch (FirebaseAuthUserCollisionException e) {
                                erroExecacao = "Conta já cadastrada";
                            } catch (Exception e) {
                                erroExecacao = "Erro ao cadastrar o usuário";
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroActivity.this,
                                    erroExecacao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
