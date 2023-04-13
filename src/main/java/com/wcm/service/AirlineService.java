package com.wcm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wcm.repository.StaffRepository;
import com.wcm.repository.WheelChairRepository;
import com.wcm.utility.StaffWheelChairFactory;
import com.wcm.utility.WcmQueue;
import com.wcm.utility.WcmSet;

@Service
public class AirlineService {
	private Queue<Object> staffQueue;
	private Queue<Object> wheelchairQueue;
	private Set<Object> wheelchairSet;
	private Set<Object> staffSet;

	@Autowired
	private WcmQueue wcmQueue;
	
	@Autowired 
	private WcmSet wcmSet;
	
	@Autowired
	private StaffRepository staffRepo;
	
	@Autowired
	private WheelChairRepository wcRepo;
	
	@Autowired
	private StationRouterService stationRouterService;
	
	@EventListener(ApplicationReadyEvent.class)
	public void createQueue() {
		this.staffQueue = wcmQueue.createQueue();
		this.staffSet = wcmSet.createSet();
		this.wheelchairQueue = wcmQueue.createQueue();
		this.wheelchairSet = wcmSet.createSet();
		// status message
		System.out.println("AI Queue created");
		System.out.println("AI Set created");
	}
	
	//Staff QUEUE
	
	// method will get Available staff from db and puth them into queue
	@Scheduled(fixedDelay = 15000, initialDelay = 2000)
	public void updateStaffQueue() {
		this.staffSet = staffRepo.getStaffSet("AI", "AVAILABLE");
		this.staffSet.removeAll(this.staffQueue);
		this.staffQueue.addAll(this.staffSet);
		this.staffSet.clear();
		System.out.println("AI_staff_hit");
	}
	
	// get the status of the queue
	public boolean getQueueStatus() {
		return this.staffQueue.isEmpty();
	}
	
	// get the first element of staff QUEUE
	public Object getStaff() {
		return this.staffQueue.remove();
	}
	
	//Wheel chair QUEUE
	
	// method will get Available staff from db and puth them into queue
	@Scheduled(fixedDelay = 15000, initialDelay = 2000)
	public void updateWheelChairQueue() {
		this.wheelchairSet = wcRepo.getWheelChairSet("AI",true);
		this.wheelchairSet.removeAll(this.wheelchairQueue);
		this.wheelchairQueue.addAll(this.wheelchairSet);
		this.wheelchairSet.clear();
		System.out.println("AI_wc_hit");
	}
	
	// get the first element of wheel chair QUEUE
	public Object getWheelChair() {
		return this.wheelchairQueue.remove();
	}
	
	// display queue's
	public void displayQueue() {
		System.out.println("AI_STAFF QUEUE DATA - "+this.staffQueue.toString());
		System.out.println("AI_WC QUEUE DATA - "+this.wheelchairQueue.toString());
	}
	
	// assigns queue and set to null;
	public void DeleteQueue() {
		this.staffQueue = null;
		this.staffSet = null;
		this.wcmQueue = null;
		this.wcmSet = null;
		System.out.println("Queue nullified");
	}
	
	// Station and Airline 
	public List<Object> RequestStation(String stCode) {
		StaffWheelChairFactory staffWheelChairFactory = stationRouterService.ForwardRequest(stCode);
		int statusCode = staffWheelChairFactory.getQueueStatus();
		List<Object> pair = new ArrayList<>();
		switch (statusCode) {
		case 3:
			pair = staffWheelChairFactory.getStaffWheelChairBasedOnCode(statusCode);
			break;
		case 2: pair = staffWheelChairFactory.getStaffWheelChairBasedOnCode(statusCode);
				pair.add(wheelchairQueue.remove());
				break;
		case 1: pair = staffWheelChairFactory.getStaffWheelChairBasedOnCode(statusCode);
				pair.add(staffQueue.remove());
				break;
		case 0: pair.add(staffQueue.remove());
				pair.add(wheelchairQueue.remove());
				break;
		}
		return pair;
	}
}
