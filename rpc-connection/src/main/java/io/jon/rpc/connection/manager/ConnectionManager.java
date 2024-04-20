package io.jon.rpc.connection.manager;

import io.jon.rpc.common.exception.RefuseException;
import io.jon.rpc.common.utils.StringUtils;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.disuse.api.DisuseStrategy;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.loader.ExtensionLoader;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private volatile Map<String, ConnectionInfo> connectionMap = new ConcurrentHashMap<>();
    private final DisuseStrategy disuseStrategy;
    private final int maxConnections;
    private static volatile ConnectionManager instance;

    private ConnectionManager(int maxConnections, String disuseStrategyType){
        this.maxConnections = maxConnections <= 0 ? Integer.MAX_VALUE : maxConnections;

        disuseStrategyType = StringUtils.isEmpty(disuseStrategyType) ?
                RpcConstants.RPC_CONNECTION_DISUSE_STRATEGY_DEFAULT : disuseStrategyType;

        this.disuseStrategy = ExtensionLoader.getExtension(DisuseStrategy.class, disuseStrategyType);
    }

    public static ConnectionManager getInstance(int maxConnections, String disuseStrategy){

        if(instance == null){
            synchronized (ConnectionManager.class){
                if (instance == null){
                    instance = new ConnectionManager(maxConnections, disuseStrategy);
                }
            }
        }
        return instance;
    }

    public void add(Channel channel){
        ConnectionInfo info = new ConnectionInfo(channel);
        if(this.checkConnectionList(info)){
            connectionMap.put(getKey(channel), info);
        }
    }

    /**
     * 检测连接列表
     */
    private boolean checkConnectionList(ConnectionInfo info) {

        List<ConnectionInfo> connectionList = new ArrayList<>(connectionMap.values());
        if(connectionList.size() >= maxConnections){
            try{
                ConnectionInfo cacheConnectionInfo = disuseStrategy.selectConnection(connectionList);
                if(cacheConnectionInfo != null){
                    cacheConnectionInfo.getChannel().close();
                    connectionMap.remove(getKey(cacheConnectionInfo.getChannel()));
                }
            }catch (RefuseException e){
                // 如果在淘汰已建立连接的channel过程中发生错误
                // 将当前准备建立连接的channel关闭
                info.getChannel().close();
                return false;
            }
        }
        return true;
    }

    public void remove(Channel channel){
        connectionMap.remove(getKey(channel));
    }

    public void update(Channel channel){
        ConnectionInfo info = connectionMap.get(getKey(channel));
        info.setLastUseTime(System.currentTimeMillis());
        info.incrementUseCount();
        connectionMap.put(getKey(channel), info);
    }

    public String getKey(Channel channel){
        return channel.id().asLongText();
    }


}
