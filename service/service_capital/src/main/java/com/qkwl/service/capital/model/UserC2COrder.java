package com.qkwl.service.capital.model;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class UserC2COrder implements Delayed{

	//订单id
	private int orderId;
	private long startTime;
    
	public UserC2COrder(){
		
	}
	
	public UserC2COrder(int orderId, int timeout){
		this.orderId = orderId;
		this.startTime = System.currentTimeMillis() + timeout*1000L;
	}
	
	@Override
	public int compareTo(Delayed other) {
		if (other == this){
			return 0;
		}
		if(other instanceof UserC2COrder){
			UserC2COrder otherRequest = (UserC2COrder)other;
			long otherStartTime = otherRequest.getStartTime();
			return (int)(this.startTime - otherStartTime);
		}
		return 0;

	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (orderId ^ (orderId >>> 32));
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		return result;
	}
 
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserC2COrder other = (UserC2COrder) obj;
		if (orderId != other.orderId)
			return false;
		if (startTime != other.startTime)
			return false;
		return true;
	}
	
	public long getStartTime() {
		return startTime;
	}
 
	public int getOrderId() {
		return orderId;
	}
 
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
 
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
 
	@Override
	public String toString() {
		return "UserC2COrder [orderId=" + orderId + ", startTime=" + startTime + "]";
	}
	
//	public static void main(String[] args) {
//	    DelayQueue<UserC2COrder> queue = new DelayQueue();
//	    queue.add(new UserC2COrder(1,10));
//	    queue.add(new UserC2COrder(2,10));
//	    queue.add(new UserC2COrder(3,10));
//
//	    System.out.println("queue put done");
//
//	    while(!queue.isEmpty()) {
//	        try {
//	        	UserC2COrder task = queue.take();
//	            System.out.println(task.orderId + ":" + System.currentTimeMillis());
//
//	        } catch (InterruptedException e) {
//	            e.printStackTrace();
//	        }
//	    }
//	}
}
