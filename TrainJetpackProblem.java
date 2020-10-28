public class TrainJetpackProblem { 
  
    private static int minHops(int nc, int[] jth, int v) 
    { 
        // safecheck
        if (v == 0 || jth[0] == 0 || nc != jth.length) 
            return -1; 
        
        // array will not exceed the value v
        int hops[] = new int[v]; 
        // init index will always be 0
        hops[0] = 0; 
        
        for (int i = 1; i < v; i++) { 
            for (int j = 0; j < i; j++) { 
                // condition to find from which to which compartment
                if (i <= j + jth[j]) { 
                    hops[i] = hops[j] + 1; 
                    break; 
                } 
            } 
        } 
        return hops[v - 1]; 
    }   
    
    public static void main(String[] args) 
    { 
        int thresholdArray[] = { 2,3,1,1,3 }; 
	int compartments = 5;
	int valueCompartment = 5;
        int minHopsvalue = minHops(compartments,thresholdArray,valueCompartment);
  
        if(minHopsvalue == -1){
            System.out.println("Incorrect input"); 
        }else{
           System.out.println("Minimum hops needed : "+minHopsvalue );  
        }
        
    } 
}