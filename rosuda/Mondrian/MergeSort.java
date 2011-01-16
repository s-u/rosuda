/**
 * An implementation of MergeSort, needs to be subclassed to provide a 
 * comparator.
 *
 * @version %I% %G%
 *
 * @author Scott Violet
 */
public class MergeSort {
    private static  double toSort[];
    private static  double swapSpace[];
    private static int[] index;
    private static int[] swapIndex;
	private static int maxLength;

	
    public static int[] sort(double array[]) {
		if(array != null && array.length > 1)
		{
			
			maxLength = array.length;
			swapSpace = new double[maxLength];
			swapIndex = new    int[maxLength];
			index = new int[maxLength];
			for( int i=0; i<maxLength; i++ )
				index[i] = i;
			toSort = array;
			mergeSort(0, maxLength - 1);
			swapSpace = null; toSort = null; swapIndex = null;
		} 
		return index;
    }
	
    private static void mergeSort(int begin, int end) {
		if(begin != end)
		{
			int mid;
			
			mid = (begin + end) / 2;
			mergeSort(begin, mid);
			mergeSort(mid + 1, end);
			merge(begin, mid, end);
		}
    }
	
    private static void merge(int begin, int middle, int end) {
		
		int firstHalf, secondHalf, count;
		
		firstHalf = count = begin;
		secondHalf = middle + 1;
		while((firstHalf <= middle) && (secondHalf <= end))
		{
			if(toSort[secondHalf] > toSort[firstHalf] ) {   // !!!!!!!!!!!! Attention: we sort the other way round !!!!!!!!!!!!!!!!
				swapSpace[count] = toSort[secondHalf];
				swapIndex[count++] = index[secondHalf++];
			} else {
				swapSpace[count] = toSort[firstHalf];
				swapIndex[count++] = index[firstHalf++];
			}
		}
		if(firstHalf <= middle)
		{
			while(firstHalf <= middle) {
				swapSpace[count] = toSort[firstHalf];
				swapIndex[count++] =  index[firstHalf++];
			}
		}
		else
		{
			while(secondHalf <= end) {
				swapSpace[count] = toSort[secondHalf];
				swapIndex[count++] =  index[secondHalf++];
			}
		}
		for(count = begin;count <= end;count++) {
			toSort[count] = swapSpace[count];
			 index[count] = swapIndex[count];
		}
    }
}