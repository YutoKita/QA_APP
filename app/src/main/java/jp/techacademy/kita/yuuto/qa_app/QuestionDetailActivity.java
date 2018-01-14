package jp.techacademy.kita.yuuto.qa_app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private FloatingActionButton fab2;

    private DatabaseReference mAnswerRef;
    private DatabaseReference dataBaseReference;
    private DatabaseReference mFavoriteRef;
    private boolean mFlag = false;
    FirebaseAuth mAuth;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //お気に入りボタン押下した時のリスナーmFavoriteListener作成
    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String str = (String)dataSnapshot.getValue();
            // isFavoriteNumber:1 str:"1"
            int isFavoriteNumber = Integer.parseInt(str);

            //dataSnapshot:"DataSnapshot{key = genre , value = 1}"
            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            //String body = (String) map.get("body");
            //String name = (String) map.get("name");
            //String uid = (String) map.get("uid");

            //Answer answer = new Answer(body, name, uid, answerUid);
            //mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        //DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //お気に入りデータを保存
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                //ファイルPATH指定
                DatabaseReference mFavoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                if (mFlag == true) {
                    fab2.setBackgroundTintList(ColorStateList.valueOf(Color.YELLOW));
                    //お気に入りしているときとしていないときで処理を分ける。フラグ作成。
                    //お気に入り解除(データ削除)
                    mFavoriteRef.removeValue();
                    //Toast.makeText(自分自身の, "メッセージ", Toast.LENGTH_SHORT).show();
                    //Snackbarを利用した場合、
                    Snackbar.make(fab2, "お気に入り登録が解除されました", Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(QuestionDetailActivity.this, "お気に入り登録されました", Toast.LENGTH_SHORT).show();
                    mFlag = false;
                } else {
                    fab2.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    Map<String, String> data = new HashMap<String, String>();
                    // UID
                    data.put("genre", String.valueOf(mQuestion.getGenre()));
                    mFavoriteRef.setValue(data);
                    //Toast.makeText(QuestionDetailActivity.this, "お気に入り登録が解除されました", Toast.LENGTH_SHORT).show();
                    Snackbar.make(fab2, "お気に入り登録されました", Snackbar.LENGTH_LONG).show();
                    mFlag = true;
                }
            }
        });
        dataBaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            //お気に入りボタンの非表示
            fab2.setVisibility(View.GONE);
        } else {
            //お気に入りボタン表示
            fab2.setVisibility(View.VISIBLE);
            //firebaseのデータ読み込み mFavoriteRef
            mFavoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            mFavoriteRef.addChildEventListener(mFavoriteListener);
        }
    }
}



