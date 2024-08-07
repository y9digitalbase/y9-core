package net.risesoft.y9public.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * FTP 客户端连接池
 *
 */
public class FtpClientPool {

    /**
     * ftp客户端连接池
     */
    private final GenericObjectPool<FTPClient> pool;

    /**
     * ftp客户端工厂
     */
    private final FtpClientFactory clientFactory;

    /**
     * 构造函数中 注入一个bean
     * 
     * @param clientFactory 客户端连接池
     */
    public FtpClientPool(FtpClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        pool = new GenericObjectPool<FTPClient>(clientFactory, clientFactory.getFtpPoolConfig());

    }

    /**
     * 借 获取一个连接对象
     * 
     * @return object instance from the pool
     * @throws Exception – if an object instance cannot be returned due to an error
     */
    public FTPClient borrowObject() throws Exception {
        return pool.borrowObject();
    }

    public FtpClientFactory getClientFactory() {
        return clientFactory;
    }

    public GenericObjectPool<FTPClient> getPool() {
        return pool;
    }

    /**
     * 还 归还一个连接对象
     * 
     * @param ftpClient object instance from the pool
     */
    public void returnObject(FTPClient ftpClient) {
        if (ftpClient != null) {
            pool.returnObject(ftpClient);
        }
    }
}