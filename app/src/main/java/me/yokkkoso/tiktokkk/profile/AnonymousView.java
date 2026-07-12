package me.yokkkoso.tiktokkk.profile;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class AnonymousView {

    private static final String[] BLOCK = {
            "/friend/visit", "/visit_report", "/visit_event/report",
            "profile_view", "/visitor"
    };

    public static void install(ClassLoader cl) {
        if (!Prefs.is(Prefs.ANONYMOUS_PROFILE_VIEW)) return;
        try {
            Class<?> builder = XposedHelpers.findClass("okhttp3.OkHttpClient$Builder", cl);
            XposedHelpers.findAndHookMethod(builder, "build", new XC_MethodHook() {
                @Override
                @SuppressWarnings("unchecked")
                protected void beforeHookedMethod(MethodHookParam param) {
                    List<Interceptor> list =
                            (List<Interceptor>) XposedHelpers.callMethod(param.thisObject, "interceptors");
                    for (Interceptor i : list) if (i instanceof VisitBlocker) return;
                    list.add(new VisitBlocker());
                }
            });
            TikToKKK.log("anonymous view interceptor armed");
        } catch (Throwable t) {
            TikToKKK.log("okhttp not hookable (ttnet may bypass), anon view off: " + t);
        }
    }

    private static final class VisitBlocker implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws java.io.IOException {
            Request req = chain.request();
            String path = req.url().encodedPath();
            for (String b : BLOCK) {
                if (path.contains(b)) {
                    return new Response.Builder()
                            .request(req)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("blocked")
                            .body(ResponseBody.create(MediaType.parse("application/json"), "{}"))
                            .build();
                }
            }
            return chain.proceed(req);
        }
    }

    private AnonymousView() {}
}
