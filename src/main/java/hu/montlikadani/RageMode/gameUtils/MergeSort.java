package hu.montlikadani.ragemode.gameUtils;

import java.util.List;

import hu.montlikadani.ragemode.scores.PlayerPoints;

public class MergeSort {

	private List<PlayerPoints> array;
	private PlayerPoints[] tempMergArr;
	private int length;

	public void sort(List<PlayerPoints> rpp) {
		this.array = rpp;
		this.length = rpp.size();
		this.tempMergArr = new PlayerPoints[length];

		doMergeSort(0, length - 1);
	}

	private void doMergeSort(int lowerIndex, int higherIndex) {
		if (lowerIndex < higherIndex) {
			int middle = lowerIndex + (higherIndex - lowerIndex) / 2;
			// Below step sorts the left side of the array
			doMergeSort(lowerIndex, middle);
			// Below step sorts the right side of the array
			doMergeSort(middle + 1, higherIndex);
			// Now merge both sides
			mergeParts(lowerIndex, middle, higherIndex);
		}
	}

	private void mergeParts(int lowerIndex, int middle, int higherIndex) {
		for (int i = lowerIndex; i <= higherIndex; i++) {
			tempMergArr[i] = array.get(i);
		}
		int i = lowerIndex;
		int j = middle + 1;
		int k = lowerIndex;
		while (i <= middle && j <= higherIndex) {
			if (tempMergArr[i].getPoints() > tempMergArr[j].getPoints()) {
				array.set(k, tempMergArr[i]);
				i++;
			} else {
				array.set(k, tempMergArr[j]);
				j++;
			}
			k++;
		}
		while (i <= middle) {
			array.set(k, tempMergArr[i]);
			k++;
			i++;
		}
	}
}
