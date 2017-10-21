import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Created by Timothy Lampen on 2017-10-16.
 */
public class Main {
    private static final int[] PAYDAY = new int[]{1,10,21,27,37,50,63,81,95};
    private static final int[] JOB_CARDS = new int[]{700,500,500,400,400,500,600,600,600,700};
    private static final int[] EXPENSES = new int[]{15,17,20,26,34,39,41,47,49,53,57,61,66,69,73,77,87,89,96,98};
    private static final int[] EXPENSE_CARDS = new int[]{300,600,300,500,300,800,300,300,300,800,200,800,400,300,200};
    private static final int[] RENT = new int[]{32,44,51,59,64,75,82,88,92};
    private static final int[] RENT_CARDS = new int[]{700,700,400,300,600,300,400};
    private static final int[] LIFE_A = new int[]{2};
    private static final int[] LIFE_B = new int[]{2,3,4,6,10};
    private static final int[] LIFE_C = new int[]{2,4,6,9,11,13,16,19,22,29,31,33,36,40,43,52,55,60,68,70,76,68,84,86,91,94};
    private static final int[] LIFE_CARDS = new int[]{100,100,100,200,200,100,100,100,200,300,200,300,200,300,200,200,100,200,200,100,100,100,100};
    private static final int[] RAFFLE_A = new int[]{3};
    private static final int[] RAFFLE_B = new int[]{1,7,9};
    private static final int[] RAFFLE_C = new int[]{5,8,23,28,45,58,62,72,80,85,93,97};
    private static final int[] RAFFLE_CARDS = new int[]{300,400,400,300,800,300,300,200,500,200,600,500,500,300,300,300};
    private static final int[] REROLL_B = new int[]{5,8};
    private static final int[] REROLL_C = new int[]{3,30,35,42,46,48,56,67,74,79,83,90};
    private static final HashMap<Integer, SpecialType> SPECIAL_A = new HashMap<>();
    private static final HashMap<Integer, SpecialType> SPECIAL_B = new HashMap<>();
    private static final HashMap<Integer, SpecialType> SPECIAL_C = new HashMap<>();
    private static final int[] PET_CARDS = new int[]{300,400,300,400,200,500,400};
    private static int rounds;
    private static HashMap<Integer, Integer> freqRounds = new HashMap<>();
    private static HashMap<Integer, Integer> freqMoney = new HashMap<>();

    private static Player p;


    private enum SpecialType {
        CHANGE_JOB, GET_PET, GET_HOUSE
    }

    public enum Route {
        A,B,C
    }

    private static final DecimalFormat df = new DecimalFormat("#.##");
    public static void main(String[] args){
        loadPlaceholders();
        for(double i = 0; i < 100000000; i++) {
            p = new Player();
            rounds = 0;
            playGame();
            System.out.println(df.format((i/100000000.0)*100.0) + "% complete the test");
        }
        LinkedList<String> data = new LinkedList<>();
        for(Integer numRounds : freqRounds.keySet()) {
            data.add(numRounds + "@" + freqRounds.get(numRounds));
        }
        try {
            saveDataToExcel("round-freq.xlsx", data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        data.clear();
        for(Integer numRounds : freqMoney.keySet()) {
            data.add(numRounds + "@" + freqMoney.get(numRounds));
        }
       try {
            saveDataToExcel("money-freq.xlsx", data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveDataToExcel(String fileName, LinkedList<String> pastData) throws IOException {
        int rowNum = 0;
        FileInputStream excelFile = new FileInputStream(new File(fileName));
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
        XSSFSheet sheet = workbook.createSheet("Stats");
        for(String entry : pastData) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (String s : entry.split("@")) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(s);
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(new File(fileName));
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void playGame(){
        while (!(p.getRoute()==Route.C && p.getSlot()>=98)) {
            playRound();
            rounds++;
        }
        if(freqRounds.containsKey(rounds)) {
            freqRounds.put(rounds, freqRounds.get(rounds)+1);
        }
        else{
            freqRounds.put(rounds, 1);
        }

        if(freqMoney.containsKey(p.getMoney())) {
            freqMoney.put(p.getMoney(), freqMoney.get(p.getMoney())+1);
        }
        else{
            freqMoney.put(p.getMoney(), 1);
        }
    }

    private static void playRound(){
        int roll = ThreadLocalRandom.current().nextInt(1,11);
        switch (p.getRoute()) {
            case A:
                if(IntStream.of(LIFE_A).anyMatch(slot -> slot==p.getSlot()+roll)) {
                    p.setSlot(p.getSlot()+roll);
                    p.setMoney(p.getMoney()+LIFE_CARDS[ThreadLocalRandom.current().nextInt(0,LIFE_CARDS.length)]);
                }
                if(IntStream.of(RAFFLE_A).anyMatch(slot -> slot==p.getSlot()+roll)) {
                    p.setSlot(p.getSlot()+roll);
                    if(p.getMoney()>100){
                        p.setMoney(p.getMoney()-100);
                        boolean b = ThreadLocalRandom.current().nextBoolean();
                        if(b) {
                            p.setMoney(p.getMoney()+RAFFLE_CARDS[ThreadLocalRandom.current().nextInt(0, RAFFLE_CARDS.length)]);
                        }
                    }
                }
                else if(p.getSlot()<SPECIAL_A.keySet().stream().findFirst().get() && p.getSlot()+roll>=SPECIAL_A.keySet().stream().findFirst().get()) {
                    doSpecialFunction(SPECIAL_A.get(SPECIAL_A.keySet().stream().findFirst().get()));
                    p.setSlot(SPECIAL_A.keySet().stream().findFirst().get());
                }
                else if(p.getSlot()+roll>=3){//dont stop the player, continue onto c
                    p.setRoute(Route.C);
                    p.setSlot(p.getSlot()+roll-3);
                    doRouteC(0, p.getSlot());
                }
                break;
            case B:
                if(IntStream.of(LIFE_B).anyMatch(slot -> slot==p.getSlot()+roll)) {
                    p.setSlot(p.getSlot()+roll);
                    p.setMoney(p.getMoney()+LIFE_CARDS[ThreadLocalRandom.current().nextInt(0,LIFE_CARDS.length)]);
                }
                if(IntStream.of(RAFFLE_B).anyMatch(slot -> slot==p.getSlot()+roll)) {
                    p.setSlot(p.getSlot()+roll);
                    if (p.getMoney() > 100) {
                        p.setMoney(p.getMoney() - 100);
                        boolean b = ThreadLocalRandom.current().nextBoolean();
                        if (b) {
                            p.setMoney(p.getMoney() + RAFFLE_CARDS[ThreadLocalRandom.current().nextInt(0, RAFFLE_CARDS.length)]);
                        }
                    }
                }
                if(IntStream.of(REROLL_B).anyMatch(slot -> slot==p.getSlot()+roll)) {
                    p.setSlot(p.getSlot() + roll);
                    playRound();
                }
                else if(p.getSlot()<SPECIAL_B.keySet().stream().findFirst().get() && p.getSlot()+roll>=SPECIAL_B.keySet().stream().findFirst().get()) {
                    doSpecialFunction(SPECIAL_B.get(SPECIAL_B.keySet().stream().findFirst().get()));
                    p.setSlot(SPECIAL_B.keySet().stream().findFirst().get());
                }
                else if(p.getSlot()+roll>=11){
                    p.setRoute(Route.C);
                    p.setSlot(0);
                }
                break;
            case C:
                final int slot = p.getSlot();
                p.setSlot(p.getSlot()+roll);
                doRouteC(slot, p.getSlot());
        }
    }

    private static void doRouteC(int origin, int roll){
        for(Map.Entry<Integer, SpecialType> entry : SPECIAL_C.entrySet()) {
            if(entry.getKey()>origin && entry.getKey()<=origin+roll) {
                switch (entry.getValue()) {
                    case GET_HOUSE:
                        p.setSlot(entry.getKey());
                        p.setRent(RENT_CARDS[ThreadLocalRandom.current().nextInt(0, RENT_CARDS.length)]);
                        doSpecialFunction(entry.getValue());
                        break;
                    case GET_PET:
                        p.setSlot(entry.getKey());
                        doSpecialFunction(entry.getValue());
                        break;
                }
            }
            else if(entry.getValue()==SpecialType.CHANGE_JOB && entry.getKey()==origin+roll) {
                doSpecialFunction(entry.getValue());//change job doesnt cancel move

            }
        }
        if(IntStream.of(EXPENSES).anyMatch(slot -> slot==p.getSlot())) {
            p.setMoney(p.getMoney()-EXPENSE_CARDS[ThreadLocalRandom.current().nextInt(0,EXPENSE_CARDS.length)]);
        }
        if(IntStream.of(RENT).anyMatch(slot -> slot==p.getSlot())) {
            p.setMoney(p.getMoney()-RENT_CARDS[ThreadLocalRandom.current().nextInt(0, RENT_CARDS.length)]);
        }
        if(IntStream.of(LIFE_C).anyMatch(slot -> slot==p.getSlot())) {
            p.setMoney(p.getMoney()+LIFE_CARDS[ThreadLocalRandom.current().nextInt(0,LIFE_CARDS.length)]);
        }
        if(IntStream.of(RAFFLE_C).anyMatch(slot -> slot==p.getSlot())) {
            if(p.getMoney()>100){
                p.setMoney(p.getMoney()-100);
                boolean b = ThreadLocalRandom.current().nextBoolean();
                if(b) {
                    p.setMoney(p.getMoney()+RAFFLE_CARDS[ThreadLocalRandom.current().nextInt(0, RAFFLE_CARDS.length)]);
                }
            }
        }
        if(IntStream.of(REROLL_C).anyMatch(slot -> slot==p.getSlot())) {
            playRound();
        }
        if(IntStream.of(PAYDAY).anyMatch(slot -> origin<slot && slot>=p.getSlot())) {
            p.setMoney(p.getMoney()+p.getJob());
        }
    }

    private static void doSpecialFunction(SpecialType type) {
        switch (type) {
            case CHANGE_JOB:
                p.setJob(JOB_CARDS[ThreadLocalRandom.current().nextInt(0, JOB_CARDS.length)]);
                break;
            case GET_PET:
                p.setMoney(p.getMoney()-PET_CARDS[ThreadLocalRandom.current().nextInt(0, PET_CARDS.length)]);
                break;
            case GET_HOUSE:
                p.setMoney(p.getMoney()-p.getRent());
                break;
        }
    }

    private static void loadPlaceholders(){
        SPECIAL_A.put(1, SpecialType.CHANGE_JOB);
        SPECIAL_B.put(11, SpecialType.CHANGE_JOB);
        SPECIAL_C.put(7, SpecialType.CHANGE_JOB);
        SPECIAL_C.put(12, SpecialType.CHANGE_JOB);
        SPECIAL_C.put(14, SpecialType.GET_PET);
        SPECIAL_C.put(18, SpecialType.CHANGE_JOB);
        SPECIAL_C.put(24, SpecialType.CHANGE_JOB);
        SPECIAL_C.put(25, SpecialType.GET_HOUSE);
        SPECIAL_C.put(38, SpecialType.CHANGE_JOB);
        SPECIAL_C.put(54, SpecialType.CHANGE_JOB);
        SPECIAL_C.put(65, SpecialType.CHANGE_JOB);
        SPECIAL_C.put(71, SpecialType.CHANGE_JOB);
    }
}
