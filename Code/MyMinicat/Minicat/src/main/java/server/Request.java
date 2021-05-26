package server;

import java.io.IOException;
import java.io.InputStream;

public class Request {

    private String method;
    private String url;
    private InputStream inputStream;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Request(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;

        int count=0;
        while(count==0){
            count=inputStream.available();
        }
        byte[] bytes=new byte[count];
        inputStream.read(bytes);
        String inputStr=new String(bytes);

        String firstLineStr = inputStr.split("\\n")[0];

        String[] strings = firstLineStr.split(" ");
        this.method=strings[0];
        this.url=strings[1];

        System.out.println("========>>>>>>>method : "+method);
        System.out.println("========>>>>>>>url : "+url);

    }

    public Request() {
    }
}
