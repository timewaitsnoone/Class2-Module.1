package server;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Bootstrap {

    private Integer port = 8080;

    private String appBase;

    public String getAppBase() {
        return appBase;
    }

    public void setAppBase(String appBase) {
        this.appBase = appBase;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void start() throws Exception {
        //加载server配置文件
        loadServerConfig();
        loadServlet();
        loadProjectServlet(appBase);

        int corePoolSize = 10;
        int maximumPoolSize = 50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler);

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("============>Minicat start on port: " + port);
// 1
//        while(true){
//            Socket socket = serverSocket.accept();
//            OutputStream outputStream = socket.getOutputStream();
//            String data="Hello Minicat!";
//            String responseText=HttpProtocolUtil.getHttpHeader200(data.getBytes().length)+data;
//            outputStream.write(responseText.getBytes());
//            socket.close();
//        }
//========================
//        while (true){
//            Socket socket = serverSocket.accept();
//            InputStream inputStream = socket.getInputStream();
//
//            Request request=new Request(inputStream);
//            Response response=new Response(socket.getOutputStream());
//
//            response.outputHtml(request.getUrl());
//
//
//            socket.close();
//        }

//        while (true) {
//            Socket socket = serverSocket.accept();
//            InputStream inputStream = socket.getInputStream();
//
//            Request request = new Request(inputStream);
//            Response response = new Response(socket.getOutputStream());
//
//            if (servletMap.get(request.getUrl()) == null) {
//                response.outputHtml(request.getUrl());
//            } else {
//                HttpServlet httpServlet = servletMap.get(request.getUrl());
//                httpServlet.service(request, response);
//            }
//
//
//            socket.close();
//        }

        System.out.println("==========>>>使用线程池改造");
        while (true) {

            Socket socket = serverSocket.accept();
            RequestProcessor requestProcessor = new RequestProcessor(socket, servletMap);
//            requestProcessor.start();
            threadPoolExecutor.execute(requestProcessor);

        }

    }

    private Map<String, HttpServlet> servletMap = new HashMap<String, HttpServlet>();
    /**
     * 加载解析web.xml，初始化Servlet
     */
    private void loadServlet() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("web.xml");
        loadServlet(resourceAsStream, null);

    }

    private void loadServerConfig(){
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            List<Element> selectNodes = rootElement.selectNodes("//Service");
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element =  selectNodes.get(i);
                // 获取服务端口号
                Element connectorElement = (Element) element.selectSingleNode("Connector");
                String port = connectorElement.attributeValue("port");
                if (port != null && !"".equals(port)) {
                    this.port = Integer.parseInt(port);
                }

                //获取webapps路径
                Element engineElement = (Element) element.selectSingleNode("Engine");
                Element hostElement = (Element) engineElement.selectSingleNode("Host");
                if (hostElement != null) {
                    String hostName = hostElement.attributeValue("name");
                    String appBase = hostElement.attributeValue("appBase");
                    if (appBase != null && !"".equals(appBase)) {
                        this.appBase = appBase;
                    }
                }

            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描路径下的包，获取HttpServlet
     */
    private void loadProjectServlet(String path) {
        File rootFile = new File(path);
        if (!rootFile.exists() || !rootFile.isDirectory()){
            return;
        }
        File[] files = rootFile.listFiles();
        if (files == null) {
            return;
        }

        /**
         * 获取到webapps目录下所有项目
         */
        try {
            for (File hostFile : files) {
                if (hostFile.isDirectory()){
                    List<String> classNames = new LinkedList<>();
                    String hostName = hostFile.getName();
                    //File[] files1 = hostFile.listFiles();
                    //获取项目下web.xml文件
                    String webXmlPath = hostFile.getPath() + "/web.xml";
                    File webXmlFile = new File(webXmlPath);
                    if (webXmlFile.exists() && webXmlFile.isFile()){
                        InputStream is = new FileInputStream(webXmlFile);
                        //加载servlet
                        loadServlet(is,hostName);
                    }

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadServlet(InputStream resourceAsStream,String hostName) {
        //InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("web.xml");
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            List<Element> selectNodes = rootElement.selectNodes("//servlet");
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element =  selectNodes.get(i);
                // <servlet-name>lagou</servlet-name>
                Element servletnameElement = (Element) element.selectSingleNode("servlet-name");
                String servletName = servletnameElement.getStringValue();
                // <servlet-class>server.LagouServlet</servlet-class>
                Element servletclassElement = (Element) element.selectSingleNode("servlet-class");
                String servletClass = servletclassElement.getStringValue();


                // 根据servlet-name的值找到url-pattern
                Element servletMapping = (Element) rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
                // /lagou
                String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
                if (hostName == null || "".equals(hostName)) {
                    servletMap.put(urlPattern, (HttpServlet) Class.forName(servletClass).newInstance());
                }else {
                    servletMap.put("/" + hostName + urlPattern, (HttpServlet) Class.forName(servletClass).newInstance());
                }

            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }



    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
