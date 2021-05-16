/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.*;
import java.util.*;

/**
 *
 * @author Emerson de Lemmus
 */
public class EmkunPrepriorityScheduler {

	
    int numProcs;
	int arrivalCheck;
	PQsort pqs;
	PriorityQueue<pib> pq;
	pib[] infoArray;
	int time;
	pib CPU;

	int numItems;
	boolean done;
	int delay = 500;
	String gantt = "";
	String fName;
	int Q;

	public static void main(String[] args)
	{
		try
		{
			EmkunPrepriorityScheduler run = new EmkunPrepriorityScheduler();
			run.start();
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	void start()
	{
		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter the time quantum integer: ");
		Q = scan.nextInt();

		Scanner fileName = new Scanner(System.in);
		System.out.print("Please enter the file name, including extension: ");
		fName = fileName.nextLine();

		System.out.println("Loading file...");
		load();
		sequence();
		displayStats();
	}

	public EmkunPrepriorityScheduler()
	{
		numProcs = 0;
		arrivalCheck = 0;
		pqs = new PQsort();
		pq = new PriorityQueue<pib>(100, pqs);
		infoArray = new pib[100];
		time = 0;
		done = false;
		CPU = null;
	}

	boolean exists()
	{
		for(int i = 0; i < numProcs; ++i)
		{
			if(infoArray[i].cpuNeeds <= 0) continue;
			return true;
		}
		return false;
	}

	public void sequence()
	{
		System.out.println("Time: " + time);
		while(exists())
		{
			if(arrivalCheck < numProcs && infoArray[arrivalCheck].arrivalTime == time)
			{
				System.out.println("     +++ Arrival:, add " + infoArray[arrivalCheck].ident + " to the priority queue with the priority " + infoArray[arrivalCheck].priority);
				infoArray[arrivalCheck].qInputTime = time;
				pq.offer(infoArray[arrivalCheck++]);
			}

			if(!pq.isEmpty())
			{
				if(CPU == null)
				{
					CPU = pq.poll();
					CPU.currBurstDone = 0;
					System.out.println("   ^^^ Empty CPU: Dispatched: " + CPU.ident + " with " + CPU.cpuNeeds + " left");
				}

				else
				{
					pib first = pq.peek();

					if(first.priority < CPU.priority)
					{
						CPU.qInputTime = time;
						pq.offer(CPU);
						CPU = pq.poll();
						CPU.currBurstDone = 0;
						System.out.println("   ^^^ Priority preempt: Dispatched: " + CPU.ident + " with " + CPU.cpuNeeds + " left");
					}

					else if(first.priority == CPU.priority && CPU.currBurstDone >= Q)
					{
						CPU.qInputTime = time;
						pq.offer(CPU);
						CPU = pq.poll();
						CPU.currBurstDone = 0;
						System.out.println("   ^^^ Quantum preempt: Dispatched: " + CPU.ident + " with " + CPU.cpuNeeds + " left");
					}
				}
			}

			String CPUWorkOn = "       >>> During this time unit the CPU is working on: ";
			CPUWorkOn = CPUWorkOn + CPU.ident;
			gantt = gantt + " | " + CPU.ident;
			System.out.println(CPUWorkOn);

			System.out.print("   Ready Queue: ");
			String readyQ = "";

			for(pib pibID: pq) 
			{
				System.out.print("" + pibID.ident);
				readyQ = readyQ + pibID.ident;
			}

			System.out.println("\n");
			char id = (char)CPU.ident;
			++time;

			if(CPU != null)
			{
				--CPU.cpuNeeds;
				++CPU.currBurstDone;
			}

			try
			{
				Thread.sleep(delay);
			}

			catch(Exception first)
			{}

			System.out.println("Time: " + time);
			if(CPU == null || CPU.cpuNeeds != 0) continue;
			System.out.println("      --- Process: " + CPU.ident + " ends at time: " + time);
			CPU.endTime = time;
			CPU = null;
		}
	}

	void displayStats()
	{
		System.out.print("\n\nFinal Stats\n");

		for(int i = 0; i < numProcs; ++i)
		{
			System.out.println("Process: " + infoArray[i].ident + " arrived at time: " + infoArray[i].arrivalTime + ", ended at time: " + infoArray[i].endTime + ", with a turnaround time of: " + (infoArray[i].endTime - infoArray[i].arrivalTime));
		}

		double total = 0;

		for(int i = 0; i < numProcs; ++i)
		{
			total = total + (infoArray[i].endTime - infoArray[i].arrivalTime);
		}

		double meanTime = total / numProcs;
		System.out.printf("\nThe average turnaround time is " + String.format("%.2f", meanTime) + " for " + numProcs + " processes\n\n");

		gantt = gantt + " | ";
		System.out.print("\nGantt Chart: " + gantt);
	}

	private void load()
	{
		try
		{
			Scanner file = new Scanner(new File(fName));
			int numItems = file.nextInt();
			int i = 0;
			char ID = 'A';
			while(file.hasNextInt())
			{
				infoArray[i] = new pib();
				infoArray[i].ident = ID;
				infoArray[i].arrivalTime = file.nextInt();
				infoArray[i].priority = file.nextInt();
				infoArray[i].cpuNeeds = infoArray[i].cpuMax = file.nextInt();
				++i;
				ID = (char)(ID + '\u0001');
			}

			numProcs = i;
			System.out.println("File loading successful. Starting the process\n");
		}

		catch(Exception e)
		{
			System.out.print("Unable to open the file, stopping program\n");
		}
	}

	static class PQsort implements Comparator<pib>
	{
		PQsort()
		{}

		@Override
		public int compare(pib one, pib two)
		{
			if(one.priority == two.priority)
			{
				return one.qInputTime - two.qInputTime;
			}
			return one.priority - two.priority;
		}
	}

	class pib
	{
		public char ident;
		public int arrivalTime;
		public int priority;
		public int cpuMax;
		public int cpuNeeds;
		public int endTime;
		public int currBurstDone;
		public int qInputTime = 0;

		pib()
		{}
	}
}
