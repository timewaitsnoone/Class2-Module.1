package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Response {

    private OutputStream outputStream;

    public Response() {
    }

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void output(String content) throws IOException {
        outputStream.write(content.getBytes());
    }



    public void outputHtml(String path) throws IOException {
        String absoluteResourcePath=StaticResourceUtil.getAbsolutePath(path);

        File file=new File(absoluteResourcePath);
        if(file.exists() && file.isFile()){
            StaticResourceUtil.outputStaticResource(new FileInputStream(file),outputStream);
        }else{
            output(HttpProtocolUtil.getHttpHeader404());
        }
    }
}
