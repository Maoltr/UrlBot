import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuthService;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class SimpleBot extends TelegramLongPollingBot {

    private static String api = "AIzaSyDbdptE7pjHYR8rJ6xtDA-Luj1k2AJij30";
    private String url;
    private int step = 0;
    private String[] strings = new String[3];

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new SimpleBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "@GeneratorUtmBot";
    }

    @Override
    public String getBotToken() {
        return "563089543:AAH5Wh8v444ChzuwISCPNRBc4TwbRQl87zU";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            if (message.getText().equals("/start") || step == 0) {
                sendMsg(message, "Привет, я робот для создания utm-меток. Для начала работы " +
                        "пришлите полное название сайта в формате http:// или https:// Для этого просто скопируйте свой сайт с адресной строки");
                step = 1;
            } else {
                logic(step, message);
            }
        }
    }

    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    void logic (int step, Message message){
        switch (step){
            case 1:
                url(message);
                break;
            case 2:
                source(message);
                break;
            case 3:
                channel(message);
                break;
            case 4:
                company(message);
                break;
            default:
                sendMsg(message, "Начните сначала(команда /start)");
        }
    }

    void url (Message message){
        if (message.getText().indexOf("http") != -1 && message.getText().indexOf("://") != -1) {
            url = message.getText();
            sendMsg(message, "Пришлите источник компании");
            step = 2;
        } else {
            sendMsg(message, "Упс, некоректный адрес сайта, скопируйте еще раз");
        }
    }

    void source (Message message){
        strings[0] = message.getText();
        sendMsg(message, "Пришлите канал компании");
        step = 3;
    }

    void channel (Message message){
        strings[1] = message.getText();
        sendMsg(message, "Пришлите название компании");
        step = 4;
    }

    void company (Message message){
        strings[2] = message.getText();
        try {
            String big = doUtm();
            String shorten = shortenUrl(big);
            sendMsg(message, "Полная ссылка: " + big);
            sendMsg(message, "Короткая ссылка: " + shorten);
            sendMsg(message, "Бот сделан по заказу канала компании in-top marketing @marketinglikbez. Нужен бот? Обращайтесь - @mmtretiak");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        step = 0;
    }

    String doUtm (){
        String result = url;
        result = result + "?utm_source=" + strings[0] + "&utm_medium=" + strings[1] + "&utm_campaign=" + strings[2];
        return result;
    }

    public static String shortenUrl(String longUrl) throws IOException, JSONException {
        @SuppressWarnings("unused")

        OAuthService oAuthService = new ServiceBuilder().apiKey("anonymus").apiSecret("anonymous")
                .scope("https://www.googleapis.com/auth/urlshortener") .build(GoogleApi20.instance());
        OAuthRequest oAuthRequest = new OAuthRequest(Verb.POST, "https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyDbdptE7pjHYR8rJ6xtDA-Luj1k2AJij30",oAuthService);
        oAuthRequest.addHeader("Content-Type", "application/json");

        //String json = "{\"longUrl\": \"http://"+longUrl+"/\"}";
        JSONObject jsonObject =new JSONObject();
        jsonObject.put("longUrl",longUrl);
        oAuthRequest.addPayload(jsonObject.toString());
        Response response = oAuthRequest.send();
        Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> responseMap = new GsonBuilder().create().fromJson(response.getBody(), typeOfMap);
        String st=responseMap.get("id");
        return st;

    }

}