package com.fowlart.main.sftp;

import com.fowlart.main.logging.LoggerHelper;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

//@Service
public class PdpImageSftp {
    private String login;
    private String password;
    private String path;

    public PdpImageSftp(@Value("${app.sftp.login}") String login,
                        @Value("${app.sftp.password}") String password,
                        @Value("${app.sftp.folder}") String path) {

        this.login = login;
        this.path = path;
        this.password = password;
    }

    @PostConstruct
    public void startServer() throws IOException {
        start();
    }

    private void start() throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();

        VirtualFileSystemFactory fileSystemFactory = new VirtualFileSystemFactory();
        fileSystemFactory.setDefaultHomeDir(Path.of(path));
        sshd.setFileSystemFactory(fileSystemFactory);

        sshd.setPort(2222);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(path+"/"+"sftp.ser")));
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        sshd.setPasswordAuthenticator(
                (username, password, session) -> username.equals(login) && password.equals(this.password));
        sshd.start();
        LoggerHelper.logInfoInFile("SFTP server for PDP images started");
    }
}
