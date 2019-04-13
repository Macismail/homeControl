/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.List;

/**
 *
 * @author 
 */
public interface ServiceObserver {

    public boolean interested(String type);

    public List<String> serviceInterests();

    public void serviceAdded(ServiceDescription service);

    public String getName();

    public void switchService(String name);

}
