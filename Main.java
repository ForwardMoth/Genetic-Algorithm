package com.company;

import java.io.*;
import java.util.*;

/***
 -----------------------------------------------------------
 bin packing problem
 using genetic algorithm
 created by ForwardMoth
 -----------------------------------------------------------
***/

class Rectangle implements Comparable<Rectangle>{
    int length;
    int width;
    int square;
    int id;
    Rectangle(int width, int length,int id) {
        this.width = width;
        this.length = length;
        this.square = getSquare();
        this.id = id;
    }
    Rectangle(int width,int length) {
        this.width = width;
        this.length = length;
        this.square = getSquare();
        this.id = -1;
    }
    public int getSquare() {
        return width*length;
    }
    @Override
    public int compareTo(Rectangle rectangle) {
        return rectangle.width - this.width;
    }
}

class CompareByFitness implements Comparator<Individual>{
    @Override
    public int compare(Individual o1, Individual o2) {
        return Float.compare(o2.fitness,o1.fitness);
    }
}

class Individual{
    int[] coefficients;
    Packer packer;
    float fitness;
    int countOfAvailableRect;
    Individual(ArrayList<Rectangle> rectangles,Rectangle container) {
        this.countOfAvailableRect = 0;
        this.coefficients = generateCoefficients(rectangles.size());
        this.packer = new Packer(getSortedRectangles(rectangles),container);
    }
    Individual(int[] coefficients,ArrayList<Rectangle> rectangles,Rectangle container) {
        this.coefficients = coefficients;
        this.countOfAvailableRect = countOfAvailableRect(rectangles);
        this.packer = new Packer(getSortedRectangles(rectangles),container);
    }
    public int[] generateCoefficients(int size) {
        int[] resultCoefficient = new int[size];
        for (int i = 0; i < size; i++) {
            int value = (int) (Math.random() * 2);
            if (value > 0) {
                resultCoefficient[i] = value;
                countOfAvailableRect++;
            }
        }
        return resultCoefficient;
    }
    public int countOfAvailableRect(ArrayList<Rectangle> rectangles) {
        int result = 0;
        for(int i=0;i<rectangles.size();i++) {
            if(coefficients[i] == 1)
                result++;
        }
        return result;
    }
    public Rectangle[] getSortedRectangles(ArrayList<Rectangle> rectangles) {
        Rectangle[] resultRectangles = new Rectangle[countOfAvailableRect];
        int j=0;
        for(int i=0;i<rectangles.size();i++){
            if(coefficients[i] > 0){
                resultRectangles[j] = rectangles.get(i);
                j++;
            }
        }
        Arrays.sort(resultRectangles);
        return resultRectangles;
    }
    public void countFitness(int square) {
        fitness = (float) packer.currentSquare / square;
    }
    public void printSolution(ArrayList<Rectangle> rectangles){
        for(int i=0;i<packer.arrayIndex.length;i++) {
            Rectangle rectangle = rectangles.get(packer.arrayIndex[i]);
            System.out.println("Rectangle #" + (i+1) + " Length: " + rectangle.length + " Width: " +rectangle.width +
                    " Coordinates: y = " + packer.coordinates[i][0] + "; x = " + packer.coordinates[i][1] + ".");
        }
        System.out.println("Area is equal to " + packer.currentSquare + ". Total square: " + (packer.W * packer.L));
    }
}

class Population{
    ArrayList<Individual> individuals;
    int sizeOfPopulation;
    ArrayList<Rectangle> rectangles;
    Rectangle container;
    Population(ArrayList<Rectangle> rectangles,Rectangle container){
        this.rectangles = rectangles;
        this.container = container;
        this.sizeOfPopulation = (rectangles.size()) * 2;
        this.individuals = generatePopulation();
    }
    Population(ArrayList<Rectangle> rectangles, Rectangle container,ArrayList<Individual> individuals) {
        this.rectangles = rectangles;
        this.container = container;
        this.sizeOfPopulation =(rectangles.size()) * 2;
        this.individuals = individuals;
    }
    Population(Population population,ArrayList<Individual> individuals2, ArrayList<Individual> individuals3){
        this.rectangles = population.rectangles;
        this.container = population.container;
        this.sizeOfPopulation = population.sizeOfPopulation + individuals2.size() + individuals3.size();
        this.individuals = unitePopulation(population.individuals,individuals2,individuals3);
    }
    public ArrayList<Individual> unitePopulation(List<Individual> ind1,List<Individual> ind2,List<Individual> ind3) {
        ArrayList<Individual> resultPopulation = new ArrayList();
        resultPopulation.addAll(ind1);
        resultPopulation.addAll(ind2);
        resultPopulation.addAll(ind3);
        return  resultPopulation;
    }
    public ArrayList<Individual> generatePopulation(){
        ArrayList<Individual> resultPopulation = new ArrayList();
        for(int i=0;i< sizeOfPopulation;i++)
            resultPopulation.add(new Individual(rectangles,container));
        return resultPopulation;
    }
    public void countOfFitnessInPopulation() {
        for(int i=0;i<sizeOfPopulation;i++) {
            individuals.get(i).packer.fillMatrix();
            individuals.get(i).countFitness(container.square);
        }
    }
    public Individual findBestSolution(){
        Individual individual = individuals.get(0);
        for(int i=0;i<sizeOfPopulation;i++) {
            if(individuals.get(i).fitness == 1.0){
                if(individual.countOfAvailableRect > individuals.get(i).countOfAvailableRect) {
                    individual = individuals.get(i);
                }
            }
            else
                break;
        }
        return individual;
    }
}

class Packer{
    int[][] matrix;
    int currentSquare;
    int L,W;
    int[][] coordinates;
    int[] arrayIndex;
    Rectangle[] availableRectangles;
    Packer(Rectangle[] rectangles,Rectangle container) {
        this.matrix= new int[container.width][container.length];
        this.currentSquare = 0;
        this.availableRectangles = rectangles;
        this.L = container.length;
        this.W = container.width;
        this.coordinates = new int[0][0];
        this.arrayIndex = new int[0];
    }
    public boolean checkCoordinate(int x,int y,Rectangle rectangle){
        int length = rectangle.length, width = rectangle.width;
        if((x-1) + length < L && (y-1) + width < W){
            for(int i=y;i<y+width;i++) {
                for(int j=x;j<x+length;j++) {
                    if(matrix[i][j] == 1)
                        return false;
                }
            }
        }
        else
            return false;
        return true;
    }
    public void paintMatrix(int x,int y,Rectangle rectangle) {
        int length = rectangle.length, width = rectangle.width;
        coordinates = Arrays.copyOf(coordinates,coordinates.length+1);
        coordinates[coordinates.length-1] = new int[2];
        coordinates[coordinates.length-1][0] = y;
        coordinates[coordinates.length-1][1] = x;
        arrayIndex = Arrays.copyOf(arrayIndex,arrayIndex.length+1);
        arrayIndex[arrayIndex.length-1] = rectangle.id;
        for(int i=y;i<y+width;i++) {
            for(int j=x;j<x+length;j++) {
                matrix[i][j] = 1;
            }
        }
        currentSquare+= length*width;
    }
    public void fillMatrix() {
        for(int i=0;i<availableRectangles.length;i++) {
            if(currentSquare+availableRectangles[i].square<=W*L){
                for(int y=0;y<W;y++) {
                    boolean flag = false;
                    for(int x=0;x<L;x++) {
                        if(matrix[y][x] == 0) {
                            if(checkCoordinate(x,y,availableRectangles[i])){
                                paintMatrix(x,y,availableRectangles[i]);
                                flag = true;
                                break;
                            }
                        }
                    }
                    if(flag == true)
                        break;
                }
            }
        }
    }
}

class Genetic{
    Population population;
    int[][] pairIndexes;
    Genetic(Population population) {
        this.population = population;
        pairIndexes = new int[population.sizeOfPopulation][2];
    }
    public boolean checkIdenticalPairs(int a,int b,int index) {
        for(int i=0;i< index;i++) {
            if((pairIndexes[i][0] == a && pairIndexes[i][1] == b) || (pairIndexes[i][0] == b && pairIndexes[i][1] == a)){
                return false;
            }
        }
        return true;
    }
    public int tournament() {
        int a = (int) (Math.random() * population.sizeOfPopulation),b=-1;
        do{
            b = (int) (Math.random() * population.sizeOfPopulation);
        }while (b == a);
        return  population.individuals.get(a).fitness > population.individuals.get(b).fitness ? a : b;
    }
    public void choosePairsOfIndividuals(){
        for(int i=0;i< population.sizeOfPopulation;i++) {
            boolean flag = true;
            while(flag) {
                int a = tournament(),b=-1;
                do {
                    b = tournament();
                }while(b == a);
                if(checkIdenticalPairs(a,b,i)){
                    flag = false;
                    pairIndexes[i][0] = a;
                    pairIndexes[i][1] = b;
                }
            }
        }
    }
    public Population crossover(){
        ArrayList<Individual> individuals = new ArrayList();
        int size = population.individuals.get(0).coefficients.length;
        for(int i=0;i< population.sizeOfPopulation;i++) {
            int value = (int) (Math.random() * (size-1));
            int[] arrayCoefficients = new int[size];
            System.arraycopy(population.individuals.get(pairIndexes[i][0]).coefficients,0,arrayCoefficients,0,
                    value+1);
            System.arraycopy(population.individuals.get(pairIndexes[i][1]).coefficients,value+1,arrayCoefficients,
                    value+1, size - (value+1));
            individuals.add(new Individual(arrayCoefficients, population.rectangles, population.container));
        }
        Population newPopulation = new Population(population.rectangles, population.container, individuals);
        return newPopulation;
    }
    public Population mutation(Population crossoverPopulation){
        ArrayList<Individual> resultIndividuals = new ArrayList();
        int size = crossoverPopulation.individuals.get(0).coefficients.length;
        for(int i=0;i<crossoverPopulation.sizeOfPopulation;i++) {
            int value = (int) (Math.random()*10 + 1);
            int[] arrayCoefficients = Arrays.copyOf(crossoverPopulation.individuals.get(i).coefficients,size);
            for(int j=0;j<size;j++) {
                int randValue = (int) (Math.random()*10 + 1);
                if (value == randValue){
                    arrayCoefficients[j] = (arrayCoefficients[j] == 1) ? 0 : 1;
                }
            }
            resultIndividuals.add(new Individual(arrayCoefficients,crossoverPopulation.rectangles,
                    crossoverPopulation.container));
        }
        Population newPopulation = new Population(crossoverPopulation.rectangles, crossoverPopulation.container,
                resultIndividuals);
        return newPopulation;
    }
    public Population selection(Population population,int N) {
        ArrayList<Individual> resultIndividuals = new ArrayList();
        int best = (int) (N * 0.8);
        int worse = N - best;
        for(int i=0;i<best;i++){
            resultIndividuals.add(population.individuals.get(i));
        }
        for(int i=population.sizeOfPopulation-1,j=0;j<worse;i--,j++){
            resultIndividuals.add(population.individuals.get(i));
        }
        Population resultPopulation = new Population(population.rectangles,population.container, resultIndividuals);
        return resultPopulation;
    }
}

public class Main {
    public static void main(String[] args) {
        try{
            final int generation = 50;
            Comparator<Individual> comparator = new CompareByFitness();
            String fileName = "second.txt";
            FileReader fReader = new FileReader(fileName);
            Scanner sc = new Scanner(fReader);
            Rectangle container = new Rectangle(sc.nextInt(), sc.nextInt());
            ArrayList<Rectangle> boxes = new ArrayList();
            int i=0;
            while(sc.hasNext()) {
                boxes.add(new Rectangle(sc.nextInt(),sc.nextInt(),i));
                i++;
            }
            if(sc != null)
                sc.close();
            if(fReader != null)
                fReader.close();
            Population firstPopulation = new Population(boxes,container);
            firstPopulation.countOfFitnessInPopulation();
            float prevGenerationFitness=0;
            int countOfRepeat=0;
            for(int g=0;g<generation && countOfRepeat < 10;g++) {
                Genetic genetic = new Genetic(firstPopulation);
                genetic.choosePairsOfIndividuals();
                Population crossoverPopulation = genetic.crossover();
                Population mutationPopulation = genetic.mutation(crossoverPopulation);
                crossoverPopulation.countOfFitnessInPopulation();
                mutationPopulation.countOfFitnessInPopulation();
                Population newPopulation = new Population(firstPopulation,crossoverPopulation.individuals,
                        mutationPopulation.individuals);
                newPopulation.individuals.sort(comparator);
                firstPopulation = genetic.selection(newPopulation, firstPopulation.sizeOfPopulation);
                if(g != generation-1) {
                    float bestFitness = newPopulation.individuals.get(0).fitness;
                    if(bestFitness == 1.0) {
                        break;
                    }
                    else {
                        if(prevGenerationFitness == bestFitness){
                            countOfRepeat++;
                        }
                        else{
                            countOfRepeat=0;
                            prevGenerationFitness = bestFitness;
                        }
                    }
                }
            }
            firstPopulation.individuals.sort(comparator);
            Individual solution = firstPopulation.findBestSolution();
            solution.printSolution(boxes);
        }
        catch (FileNotFoundException ex1) {
            System.out.println("File is not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

