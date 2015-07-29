/**
 * jvmtop - java monitoring for the command-line
 *  
 * Copyright (C) 2015 by Patric Rufflar. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.jvmtop;

import java.util.Map;

import com.jvmtop.monitor.VMInfo;
import com.jvmtop.openjdk.tools.LocalVirtualMachine;


/**
 * GetJvmMemUtilization - get memory utilization as % for the list of PID's passed as args
 * This class added by kk (kmkale@gmail.com)
 * 
 * This class takes a list of JVM PID's as args and prints out each JVMS memory utilization in %. If the JVM can't be reached it prints 0
 * My purpose in writing this class is to use it in raising alerts if the memory utilization starts approaching a set limit.
 *
 * @author kk (kmkale@gmail.com)
 *
 */
public class GetJvmMemUtilization
{
  
  /**
   * @param args
   * space separated list of JVM PID's we are interested in
   */
  public static void main(String[] args)
  {
    if(args.length == 0){
      System.out.println("GetJvmMemUtilization - java class for getting %mem utilization of listed JVM's");
      System.out.println("Usage: jvmmemutil.sh PID [PID...] (At least one PID. PID=pid of the JVM.)");
      System.exit(0);
    }
    
    Map< Integer, LocalVirtualMachine > map = LocalVirtualMachine.getAllVirtualMachines(); //lets get all JVM's we can find 
    for(String s:args){
      try{
        int vmid = Integer.parseInt(s);
        LocalVirtualMachine lvm = map.get(vmid);
        VMInfo vmInfo_ = VMInfo.processNewVM(lvm, vmid);
        
        //for debugging
        /*System.out.printf(
            " CPU: %5.2f%% GC: %5.2f%% HEAP:%5s /%5s NONHEAP:%5s /%5s%n",
            vmInfo_.getCpuLoad() * 100, vmInfo_.getGcLoad() * 100,
            toMB(vmInfo_.getHeapUsed()), toMB(vmInfo_.getHeapMax()),
            toMB(vmInfo_.getNonHeapUsed()), toMB(vmInfo_.getNonHeapMax()));*/
        
        System.out.print(vmid+"="+Math.round(vmInfo_.getHeapUsed()*100/vmInfo_.getHeapMax())+" ");
      }
      catch(NumberFormatException nfe){
        //ignore
        //System.out.println("PID should be given as number only. Input "+s+" is invalid");
      }      
    }
    System.out.println();
  }
  
  /**
   * Formats a long value containing "number of bytes" to its megabyte representation.
   * If the value is negative, "n/a" will be returned.
   *
   * TODO: implement automatic scale to bigger units if this makes sense
   * (e.g. output 4.3g instead of 4324m)
   *
   * @param bytes
   * @return
   */
  public static String toMB(long bytes)
  {
    if(bytes<0)
    {
      return "n/a";
    }
    return "" + (bytes / 1024 / 1024) + "m";
  }

}
