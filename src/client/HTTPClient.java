package client;

import java.net.*;
import java.io.*;
import java.util.*;

public class HTTPClient {
    private final String CRLF = "\r\n";

    private int port;
    private String host;
    private String path = "";
    private String pathName = "";
    private Socket socket;
    private String method;
    private String query = "";
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private DataInputStream dataInputStream;

    public String getPathName() {
        return pathName;
    }

    public Socket getSocket() {
        return socket;
    }

    public HTTPClient(String host, int port){
        socket = new Socket();
        this.host = host;
        this.port = port;
    }

    public  HTTPClient(){

    }

    /**
     * 开始运行
     * @param request
     */
    public void run(String request){
        this.socket = new Socket();
        Scanner scanner = new Scanner(System.in);
        request = request.trim();
        if(request.indexOf("http")==0){
            analysisURL(request);
            this.method = "get";
        }else if(request.toLowerCase().equals("get") || request.toLowerCase().equals("post")){
            this.method = request.toLowerCase();
            System.out.print("URL: ");
            String url = scanner.nextLine();
            analysisURL(url);
        }else{
            try {
                this.socket = null;
                System.err.print("输入错误，请重新输入: ");
                String s = scanner.nextLine();
                if(!s.equals("%")) {
                    run(s);
                }else {
                    this.method = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            if(this.method.equals("get")){
                sendGet();
            }else if(this.method.equals("post")) {
                System.out.println("请输入参数，格式：属性名=属性值 多个用&连接");
                String parameter = scanner.nextLine();
                sendPost(parameter);
            }
            this.method = "";
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void analysisURL(String URL){
        this.host = URL.split("://")[1].split("/")[0];
        if(this.host.contains(":")){                //包含端口
            this.port = Integer.valueOf(this.host.split(":")[1]);
            this.host = this.host.split(":")[0];
        }else {
            this.port = 8080;                       //默认端口
        }
        int beforePath = URL.split("://")[1].indexOf("/");
        if(beforePath >= 0) {
            this.path = URL.split("://")[1].substring(beforePath);
            if(this.path.contains("?")){
                String pathAndQuery = this.path;
                this.path = pathAndQuery.split("\\?")[0];
                this.query = pathAndQuery.split("\\?")[1];
            }
            this.pathName = this.pathName + this.path.substring(1);
        }else{
            this.path = "/index.html";
            this.pathName = "/index.html";
        }
    }

    /**
     * 封装get的请求头信息
     * @throws Exception
     */
    public void sendGet() throws Exception{
        SocketAddress socketAddress = new InetSocketAddress(this.host,this.port);
        socket.connect(socketAddress);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedWriter = new BufferedWriter(outputStreamWriter);

        bufferedWriter.write("GET " + this.path + this.query + " HTTP/1.1" + CRLF);
        bufferedWriter.write("Host: " + this.host + CRLF);
        bufferedWriter.write(CRLF);
        bufferedWriter.flush();

        Receive receive = new Receive(this);
        receive.receiveFile();


    }

    /**
     * 封装post的请求头信息
     * @throws Exception
     */
    public void sendPost(String parameter) throws Exception{


        SocketAddress socketAddress = new InetSocketAddress(this.host,this.port);
        socket.connect(socketAddress);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedWriter = new BufferedWriter(outputStreamWriter);

        bufferedWriter.write("POST " + this.path  + " HTTP/1.1" + CRLF);
        bufferedWriter.write("Host: " + this.host + CRLF);
        bufferedWriter.write("Content-Length: " + parameter.length() + CRLF);
        bufferedWriter.write("Content-Type: application/x-www-form-urlencoded" + CRLF);
        bufferedWriter.write(CRLF);
        parameter = (this.query.equals(""))?parameter:parameter+"&"+this.query;
        bufferedWriter.write(parameter);
        bufferedWriter.flush();
        bufferedWriter.write(CRLF);
        bufferedWriter.flush();

        //暂时当做get处理
        Receive receive = new Receive(this);
        receive.receiveFile();

    }

    public void setPathName(String path){
        this.pathName = path;
    }
    public static void main(String[] args){

        System.out.println("客户端已启动，需输入http请求");
        System.out.println("http请求分为两种：");
        System.out.print("1. 直接输入URL； ");
        System.out.println("2. 交互式输入：先输入方法（Get/Post），再输入URL，参数");
        System.out.print("请输入： ");
        Scanner scanner = new Scanner(System.in);
        String s = "%";

        while (!(s = scanner.nextLine()).equals("%")){
            HTTPClient httpClient = new HTTPClient();
            httpClient.run(s);
            System.out.print("请输入（结束连接请输入%）： ");
        }
    }
}
