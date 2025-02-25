package com.gpfei.graduationproject.ui.activities.common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.gpfei.graduationproject.R;
import com.gpfei.graduationproject.beans.DayBean;
import com.gpfei.graduationproject.beans.HrUser;
import com.gpfei.graduationproject.beans.MyUser;
import com.gpfei.graduationproject.beans.SelectAndResume;
import com.gpfei.graduationproject.beans.User;
import com.gpfei.graduationproject.ui.activities.MessageActivity;
import com.gpfei.graduationproject.ui.activities.common.login.LoginAndRegisterActivity;
import com.gpfei.graduationproject.ui.activities.hr.HrDataActivity;
import com.gpfei.graduationproject.utils.ToastUtils;
import com.hyphenate.easeui.EaseConstant;
import com.longsh.optionframelibrary.OptionCenterDialog;
import com.mob.MobSDK;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.http.I;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class JobWebDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_back;
    private TextView tv_title;
    private WebView mWebView;
    private LinearLayout ll_error_state;
    private ImageView iv_sharing;
    private LinearLayout ll_collect;
    private Button btn_load;
    private TextView tv_collect;
    private String objectId;
    String title;
    String url;
    private TextView tv_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_web_details);
        objectId = (String) getIntent().getExtras().get("objectId");
        initView();
        loadWeb();
    }

    private void initView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //查找Person表里面id为6b6c11c537的数据
                BmobQuery<DayBean> bmobQuery = new BmobQuery<DayBean>();
                bmobQuery.getObject(objectId, new QueryListener<DayBean>() {
                    @Override
                    public void done(DayBean object,BmobException e) {
                        if(e==null){
                            title = object.getTitle_day();
                            url = object.getUrl();
                        }else{
                        }
                    }
                });

            }
        }).start();

        MobSDK.submitPolicyGrantResult(true, null);
        iv_back = findViewById(R.id.iv_back);
        tv_title = findViewById(R.id.tv_title);
        ll_collect = findViewById(R.id.ll_collect);
        tv_check = findViewById(R.id.tv_check);

        ll_error_state = findViewById(R.id.ll_error_state);
        mWebView = findViewById(R.id.mWebView);
        iv_sharing = findViewById(R.id.iv_sharing);
        btn_load = findViewById(R.id.btn_load);
        tv_collect = findViewById(R.id.tv_collect);
        btn_load.setOnClickListener(this);
        tv_title.setText("返回");
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iv_sharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
        tv_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    BmobQuery<DayBean> bmobQuery = new BmobQuery<DayBean>();
                    bmobQuery.getObject(objectId, new QueryListener<DayBean>() {
                        @Override
                        public void done(DayBean dayBean, BmobException e) {
                            if (e==null){
                                String authorId = dayBean.getAuthor().getObjectId();
                                new Thread(){
                                    @Override
                                    public void run() {
                                        equal(authorId);
                                    }
                                }.start();
                            }else {

                            }
                        }
                    });

                }
        });
        ll_collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BmobUser bmobUser = BmobUser.getCurrentUser(MyUser.class);
                if (bmobUser != null) {
                    BmobQuery<DayBean> bmobQuery = new BmobQuery<DayBean>();
                    bmobQuery.getObject(objectId, new QueryListener<DayBean>() {
                        @Override
                        public void done(DayBean object, BmobException e) {
                            if (e == null) {
                                String authorId = object.getAuthor().getObjectId();
                                new Thread() {
                                    @Override
                                    public void run() {
                                        //查找Person表里面id为6b6c11c537的数据
                                        BmobQuery<User> bmobQuery = new BmobQuery<User>();
                                        bmobQuery.getObject(authorId, new QueryListener<User>() {
                                            @Override
                                            public void done(User object, BmobException e) {
                                                if (e == null) {
                                                    String author = object.getUsername();
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Log.d("author>>>>>>>>", author);
                                                            Intent chat = new Intent(JobWebDetailsActivity.this, MessageActivity.class);
                                                            chat.putExtra(EaseConstant.EXTRA_USER_ID, author);  //对方账号
                                                            chat.putExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE); //单聊模式
                                                            startActivity(chat);
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(JobWebDetailsActivity.this, "查询失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }.start();
                            } else {
                                Toast.makeText(JobWebDetailsActivity.this, "查找失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    ToastUtils.showTextToast(JobWebDetailsActivity.this, "请先登录!");
                    startActivity(new Intent(JobWebDetailsActivity.this, LoginAndRegisterActivity.class));
                }
            }
        });

    }

    private void share() {
        OnekeyShare oks = new OnekeyShare();
        // title标题，微信、QQ和QQ空间等平台使用
        //oks.setTitle(getString(R.string.share));
        // titleUrl QQ和QQ空间跳转链接
        oks.setTitleUrl(url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(title+"招聘！");
        // setImageUrl是网络图片的url
        //oks.setImageUrl("https://hmls.hfbank.com.cn/hfapp-api/9.png");
        // url在微信、Facebook等平台中使用
        oks.setUrl(url);
        // 启动分享GUI
        oks.show(MobSDK.getContext());
    }

    private void loadWeb() {
        String url = (String) getIntent().getExtras().get("url");
        if (url != null) {
            mWebView.loadUrl(url);
//            new Thread() {
//                public void run() {
//                    //执行耗时操作
//                    queryOne(objectId);
//                }
//            }.start();
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //加载开始
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //加载结束
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //加载页面的服务器出错时调用
                mWebView.setVisibility(View.GONE);
                ll_error_state.setVisibility(View.VISIBLE);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
    }

    private void queryOne(String objectId) {
        //查找Person表里面id为6b6c11c537的数据
        BmobQuery<SelectAndResume> bmobQuery = new BmobQuery<SelectAndResume>();
        bmobQuery.getObject(objectId, new QueryListener<SelectAndResume>() {
            @Override
            public void done(SelectAndResume object, BmobException e) {
                if (e == null) {
                    if (object.getCollect()) {
                        tv_collect.setText("已收藏");
                    } else {
                        tv_collect.setText("收藏");
                    }
                } else {
                    Toast.makeText(JobWebDetailsActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_load:
                initView();
                loadWeb();
                break;
        }
    }

    /**
     * name为football的类别
     * @param authorId
     */
    private void equal(String authorId) {
        BmobQuery<HrUser> categoryBmobQuery = new BmobQuery<>();
        categoryBmobQuery.addWhereEqualTo("userInfo", authorId);
        categoryBmobQuery.findObjects(new FindListener<HrUser>() {
            @Override
            public void done(List<HrUser> object, BmobException e) {
                if (e == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String companyId = object.get(0).getObjectId();
                            startActivity(new Intent(JobWebDetailsActivity.this, CheckHrActivity.class).putExtra("companyId",companyId));
                        }
                    });

                } else {
                    Log.e("BMOB", e.toString());
                }
            }
        });
    }
}