package server;

import java.io.IOException;

public class LagouServletDemo1 extends HttpServlet{
    @Override
    public void doGet(Request request, Response response) {
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        String context="<h1>Lagou Servlet Get Demo1</h1>";
        try {
            response.output((HttpProtocolUtil.getHttpHeader200(context.getBytes().length)+context));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(Request request, Response response) {
        String context="<h1>Lagou Servlet Post</h1>";
        try {
            response.output((HttpProtocolUtil.getHttpHeader200(context.getBytes().length)+context));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void destory() throws Exception {

    }
}
