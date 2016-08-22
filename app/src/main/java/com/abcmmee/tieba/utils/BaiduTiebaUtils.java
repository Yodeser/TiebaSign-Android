package com.abcmmee.tieba.utils;

import android.content.Context;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.util.Log;

import com.abcmmee.tieba.database.TiebaSQLiteDao;
import com.abcmmee.tieba.model.Tieba;
import com.abcmmee.tieba.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BaiduTiebaUtils {
    public static final String TAG = "BaiduTiebaUtils";

    private Context mContext;
    private static BaiduTiebaUtils mBaiduTiebaUtils;

    /**
     * 私有化构构造器
     */
    private BaiduTiebaUtils(Context context) {
        mContext = context;
    }

    public static BaiduTiebaUtils getInstance(Context context) {
        if (mBaiduTiebaUtils == null)
            mBaiduTiebaUtils = new BaiduTiebaUtils(context);
        return mBaiduTiebaUtils;
    }

    /**
     * 获取登录后的Cookies
     */
    public boolean login(String account, String password) throws IOException, JSONException {

        // 创建一个cookies容器，因为获取token参数时需要cookies
        MyCookieJar cookieJar = new MyCookieJar();

        // 打开百度贴吧手机版网页，然后获取到cookies（其实www.baidu.com也可以，只不过手机版省流量）
        HttpUtils.sendGetRequest("http://tieba.baidu.com/mo/", cookieJar);

        // 第二步获取token
        Response response = HttpUtils.sendGetRequest("https://passport.baidu.com/v2/api/?getapi&tpl=pp&apiver=v3", cookieJar);

        // 解析响应的json，拿到token
        JSONObject data = new JSONObject(response.body().string()).getJSONObject("data");
        String token = data.getString("token");

        // 登录百度账号时需要POST的参数
        RequestBody formBody = new FormBody.Builder()
                .add("staticpage", "https://passport.baidu.com/static/passpc-account/html/V3Jump.html")
                .add("token", token)
                .add("tpl", "pp")
                .add("username", account)
                .add("password", password)
                .add("loginmerge", "true")
                .add("mem_pass", "on")
                .build();

        Headers headers = new Headers.Builder()
                .set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .set("Host", "passport.baidu.com")
                .set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .build();

        // 登录百度账号
        HttpUtils.sendPostRequest("https://passport.baidu.com/v2/api/?login", cookieJar, formBody, headers);

        // 检查cookies中是否有BDUSS参数，有说明登录成功
        Iterator<Cookie> iterator = cookieJar.cookies.iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();

            if (cookie.name().equals("BDUSS")) {
                // 获取BDUSS
                String bduss = cookie.value();

                // 获取用户名
                response = HttpUtils.sendGetRequest("http://tieba.baidu.com/f/user/json_userinfo/", cookieJar);
                byte[] bytes = response.body().bytes();
                JSONObject result = new JSONObject(new String(bytes, "GBK"));
                String uid = result.getJSONObject("data").getString("open_uid");
                String name = result.getJSONObject("data").getString("user_name_show");

                // 存入数据库
                User user = new User(uid, name, bduss);
                TiebaSQLiteDao dao = TiebaSQLiteDao.getInstance(mContext);
                dao.addUser(user);

                return true;
            }
        }

        return false;
    }

    /**
     * 网页签到，先放着，以后扩展功能的时候可能用到
     */
    public void signTiebaWithWepPage(User user, Tieba tieba) throws IOException, JSONException {
        // cookies中加入bduss参数
        MyCookieJar cookieJar = addBduss(user.getBduss());

        // 签到时需要POST的参数
        RequestBody requestBody = new FormBody.Builder()
                .add("ie", "utf-8")
                .add("kw", tieba.getName())
                .build();

        Response response = HttpUtils.sendPostRequest("http://tieba.baidu.com/sign/add", cookieJar, requestBody);

        String json = new String(response.body().bytes(), "unicode");
        JSONObject result = new JSONObject(json);
        if (result.getInt("no") == 0) {
            // 签到成功
        } else if (result.getInt("no") == 1101) {
            // 亲，你之前已经签过了

        } else {
            // 签到失败

        }
    }

    /**
     * 客户端签到
     *
     * @param user
     * @param tieba
     * @return 返回0表示已经签到，1表示签到成功，2表示签到错误
     * @throws IOException
     * @throws JSONException
     */
    public int signTiebaWithClient(User user, Tieba tieba) throws IOException, JSONException {
        String bduss = user.getBduss();
        MyCookieJar cookieJar = addBduss(bduss); // cookies中加入bduss参数

        // 获取tbs参数
        Response response = HttpUtils.sendGetRequest("http://tieba.baidu.com/dc/common/tbs", cookieJar);
        String tbs = new JSONObject(response.body().string()).getString("tbs");

        // 生成POST的参数
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("BDUSS", bduss);
        treeMap.put("_client_type", "4");
        treeMap.put("_client_version", "1.2.1.17");
        treeMap.put("kw", tieba.getName());
        treeMap.put("net_type", "3");
        treeMap.put("tbs", tbs);

        FormBody.Builder builder = new FormBody.Builder();
        StringBuffer sb = new StringBuffer();

        Set<Map.Entry<String, String>> set = treeMap.entrySet();
        for (Map.Entry<String, String> entry : set) {
            builder.add(entry.getKey(), entry.getValue());
            sb.append(entry.getKey() + "=" + entry.getValue());
        }

        String SIGNKEY = "tiebaclient!!!";
        String sign = Md5Encoder.encode(sb.toString() + SIGNKEY);

        RequestBody requestBody = builder.add("sign", sign).build();


        // 执行签到
        response = HttpUtils.sendPostRequest("http://c.tieba.baidu.com/c/c/forum/sign/", cookieJar, requestBody);
        String json = response.body().string();

        JSONObject result = new JSONObject(json);
        String errorCode = result.getString("error_code");
        if (errorCode.equals("0")) {
            tieba.setStatus(true); // 更新签到状态

            TiebaSQLiteDao dao = TiebaSQLiteDao.getInstance(mContext);
            dao.addTieba(tieba); // 存入数据库

            return 1;// 返回1表示签到成功
        } else if (errorCode.equals("160002")) {
            return 0; // 返回0表示已经签到
        } else {
            return 2; // 返回2表示签到错误
        }
    }

    /**
     * 获取某个账户已关注的贴吧信息，然后存入数据库
     */
    public boolean getLikeForum(User user) throws IOException, JSONException {
        // 把BDUSS参数添加到cookies中
        MyCookieJar cookieJar = addBduss(user.getBduss());

        // 获取已关注的贴吧
        Response response = HttpUtils.sendGetRequest("http://tieba.baidu.com/p/getLikeForum?uid=" + user.getUid(), cookieJar);

        List<Tieba> tiebas = new ArrayList<>();
        JSONArray array = new JSONObject(response.body().string()).getJSONObject("data").getJSONArray("info");
        for (int i = 0; i < array.length(); i++) {
            JSONObject info = array.getJSONObject(i);
            String name = info.getString("forum_name");
            String level = info.getString("user_level");
            String exp = info.getString("user_exp");
            String fid = info.getString("id");
            Tieba tieba = new Tieba(user.getUid(), name, level, exp);
            tieba.setFid(fid);
            tiebas.add(tieba);
        }

        TiebaSQLiteDao dao = TiebaSQLiteDao.getInstance(mContext);
        dao.addAllTieba(tiebas);

        return true;
    }

    /**
     * 把bduss参数添加到cookies容器中
     *
     * @param bduss
     * @return 返回一个带有bduss参数的cookies容器
     */
    private MyCookieJar addBduss(String bduss) {
        // 把BDUSS参数添加到cookies中
        MyCookieJar cookieJar = new MyCookieJar();
        Cookie cookie = new Cookie.Builder()
                .name("BDUSS")
                .value(bduss)
                .domain("baidu.com")
                .build();
        cookieJar.cookies.add(cookie);

        return cookieJar;
    }

}
