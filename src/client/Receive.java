package client;

import java.io.*;

/**
 * 接受文件时调用
 */
public class Receive {
    private String uri;
    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private String statusCode;
    private HTTPClient httpClient;
    private final String CRLF = "\r\n";

    public Receive(HTTPClient httpClient){
        this.uri = httpClient.getPathName();
        try {
            this.inputStream = httpClient.getSocket().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void receiveFile(){
        FileOutputStream fileOutputStream = null;

        byte[] inputByte = new byte[1024];

        BufferedInputStream bufferedInputStream = null;
        bufferedInputStream = new BufferedInputStream(this.inputStream);

        dataInputStream = new DataInputStream(bufferedInputStream);


        try {
            dataInputStream.read(inputByte);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputString = null;
        inputString = new String(inputByte);

        String[] lines = inputString.split(CRLF);
        statusCode = lines[0].split(" ")[1];
        for(int i = 0; i < 6; i++){
            System.out.println(lines[i]);
        }

        if(statusCode.equals("200")) {
            String fileName = this.uri;
            try {
                if(fileName.contains("/")){
                   int size = fileName.split("/").length;
                   fileName = fileName.split("/")[size-1];
                }
                fileOutputStream = new FileOutputStream(new File(fileName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            for (int i = 6; i < lines.length; i++) {
                try {
                    fileOutputStream.write((lines[i] + CRLF).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                fileOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(statusCode.equals("301")){
            System.out.println(lines[6]);
            System.out.println("所请求的文件在"+lines[6]+"目录下");
        }
    }
}
