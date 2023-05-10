package com.dhl.cmdb.api;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.FileUtils;
import com.dhl.fin.api.common.util.*;
import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 4/11/2020
 */
public class JappTest {

    @Autowired
    private FreeMarkerConfigurer freemarkerConfigurer;

    @Test
    public void test() throws IOException {

        List<File> files = Files.list(Paths.get("\\\\199.40.10.32\\it_data$\\FAA Team\\Share\\Todo\\30-UAM\\2022Q2\\AD & BCA"))
                .filter(p -> p.toFile().getName().contains("ConfirmedUsers"))
                .map(p -> p.toFile()).collect(Collectors.toList());

        System.out.println(files.get(files.size() - 1).getName());
    }


    /**
     * @throws NoSuchFieldException
     */
    @Test
    public void testDeclNumber() throws NoSuchFieldException {


        String[] s = new String[]{"1037850181", "1688155814"};
        Map datas = MapUtil.builder().add("hawbs", s).add("lastDays", 6000).build();
        System.out.println(HttpUtil.post("http://23.156.5.119:8079/api/cdms/getDeclNosByHawb", JSONUtil.toJsonStr(datas)));


    }


    @Test
    public void test1() throws IOException {
        String sql = "\\\\\\\\199.40.10.32\\\\it_data$\\\\FAA Team\\\\Share\\\\Todo\\\\30-UAM\\\\2022Q2\\\\" + "AD & BCA";
        Files.list(Paths.get(sql));

    }

    @Test
    public void testPutHadoop() throws IOException {
        byte[] filebyte = FileUtil.readBytes("C:\\Works\\Beck\\记事本.txt");
        String fileName = SecUtil.md5Encrypt(filebyte);
        String result = HttpRequest
                .put("http://23.156.6.45:10080/services/file/put/caceinvoice?filename=file_20200808.txt")
                .body(filebyte)
                .header("Content-Type", "application/octet-stream")
                .execute().body();
    }

    @Test
    public void testGetHadoop() throws IOException {
        byte[] filebyte = FileUtil.readBytes("C:\\Works\\Beck\\记事本.txt");
        String fileName = SecUtil.md5Encrypt(filebyte);
        String result = HttpRequest
                .put("http://23.156.6.45:10080/services/file/put/caceinvoice?filename=" + fileName + "_20200808")
                .body(filebyte)
                .header("Content-Type", "application/octet-stream")
                .execute().body();
    }

    @Test
    public void testDeleteHadoop() throws IOException {
        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sendTime = fullSdf.format(new Date());
        String fileName = sendTime + "&" + "68a212feadafaf762f14960c5a878e1a0d9344f5a3540d0511" + "@" + "text_20200808.txt";
        String accessToken = DigestUtils.md5Hex((DigestUtils.md5Hex(fileName)));
        String serialNo = String.valueOf(System.currentTimeMillis()).substring(1);
        System.out.println("accessToken: " + accessToken);
        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<req:Request xmlns:req=\"http://www.cn.dhl.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "             xsi:schemaLocation=\"http://www.cn.dhl.com TrackingRequestKnown.xsd\">" +
                "    <RequestHeader>" +
                "        <SendTime>" + sendTime + "</SendTime>" +
                "        <SerialNo>" + serialNo + "</SerialNo>" +
                "        <ChannelName>5idhl</ChannelName>" +
                "        <AccessToken>" + accessToken + "</AccessToken>" +
                "    </RequestHeader>" +
                "    <FileName>text_20200808.txt</FileName>" +
                "    <Type>caceinvoice</Type>" +
                "</req:Request>";
        System.out.println("fileName: " + fileName);
        String result = HttpRequest
                .post("http://23.156.6.45:10080/services/file/delete")
                .body(body)
                .header("Content-Type", "application/xml")
                .header("cache-control", "no-cache")
                .execute().body();

        System.out.println(result);


    }


    @Test
    public void testPowerShell() throws IOException, PowerShellExecutionException {
        System.out.println(String.format("'%%s%%'", "dd"));
    }


    @Test
    public void desUtils() throws IOException {

//        System.out.println(com.dhl.fin.api.common.util.DateUtil.differentDays( new Date(),com.dhl.fin.api.common.util.DateUtil.getFullDate("2021-04-12 10:49:00")));

        System.out.println(SecUtil.encrypt("GmCNgjxZg3uFsyk13KsNmKANa"));

//        FileUtils.delete(new File("\\\\23.156.6.12\\UserReview\\05_uploadFile\\ITconfirm"));

    }

    @Test
    public void copyFileComputer() throws IOException {


    }

//    @Test
//    public void copyFileLinuxComputer() throws IOException {
//        JSch jsch = new JSCH();
//        Session session = jsch.getSession(config.getUsername(), config.getHostname(), 22);
//        session.setPassword(config.getPassword());
//
//        session.connect();
//
//        Channel channel = session.openChannel("sftp");
//        channel.connect();
//        ChannelSftp cFTP = (ChannelSftp) channel;
//
//        String sourceFile = "---", targetFile = "---";
//        cFTP.put(sourceFile , targetFile );
//
//        cFTP.disconnect();
//        session.disconnect();
//    }


}





