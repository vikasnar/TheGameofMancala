import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

/**
 * Created by VikasN on 10/7/15.
 */

public class mancala {
    static int max = Integer.MIN_VALUE;
    static int min = Integer.MAX_VALUE;
    static mancalaBoard opt;
    static String traverseLog = "Node,Depth,Value\n";
    static String next_state = "";
    public static void main(String[] cmdInput) {
        String fName = null;
        String outString = "";
        int task_no;
        int active_player;
        int cut_off_level;
        if (cmdInput.length > 1) {
            fName = cmdInput[1].toString();
            if (!fName.substring(fName.length() - 4, fName.length()).equalsIgnoreCase(".txt")) {
                fName += ".txt";
            }
            try {
                File inputFile = new File(fName);
                BufferedReader inRead1 = new BufferedReader(new FileReader(inputFile));
                //Read task to perform
                String l = inRead1.readLine();
                task_no = Integer.parseInt(l);

                //Read player to play as
                l = inRead1.readLine();
                active_player = Integer.parseInt(l);

                //Read cut-off depth
                l = inRead1.readLine();
                cut_off_level = Integer.parseInt(l);

                //Read the board state
                //p2
                l = inRead1.readLine().toString();
                String[] splited = l.split("\\s+");
                int size = splited.length;
                int p2[] = new int[size + 1];
                int k = 0;
                for (int j = 1; j < size + 1; j++) {
                    p2[j] = Integer.parseInt(splited[k++]);
                }
                //p1
                l = inRead1.readLine().toString();
                splited = l.split("\\s+");
                size = splited.length;
                int p1[] = new int[size + 1];
                for (int j = 0; j < size; j++) {
                    p1[j] = Integer.parseInt(splited[j]);
                }
                //Read p2 mancala
                l = inRead1.readLine();
                int p2_m = Integer.parseInt(l);
                p2[0] = p2_m;

                //Read p1 mancala
                l = inRead1.readLine();
                int p1_m = Integer.parseInt(l);
                p1[size] = p1_m;

                mancalaBoard initial_state = new mancalaBoard(size, p1, p2, p1_m, p2_m);
                initial_state.printBoard();
                System.out.println(initial_state.getPlayer_score(1));

                switch (task_no) {
                    case 1: {
                        opt = new mancalaBoard (initial_state);
                        if (active_player == 2) {
                            initial_state = reversePlayer(initial_state);
                            greedySearch(initial_state, active_player);
                            outString += reversePlayer(opt).printBoard();
                            outString += "\n" + opt.player2[0];
                            outString += "\n" + opt.player1[opt.no_pits];
                        } else {
                            greedySearch(initial_state, active_player);
                            outString += opt.printBoard();
                            outString += "\n" + opt.player2[0];
                            outString += "\n" + opt.player1[opt.no_pits];
                        }
                        writeToFile(outString,1);
                    }
                    break;
                    case 2:{
                        opt = new mancalaBoard (initial_state);
                        minmax(initial_state,cut_off_level,active_player);
                    }
                    break;
                    case 3:{
                        opt = new mancalaBoard(initial_state);
                        alphaBetaPruning(initial_state,cut_off_level,active_player);
                    }
                    break;
                }
            } catch (Exception e) {
                writeToFile(outString,1);
                System.out.println("File read exception");
                e.printStackTrace();
            }
        }
    }

    public static void writeToFile(String s,int id){
        try {
            PrintWriter writer;
            if(id == 1){
                writer = new PrintWriter("next_state.txt", "UTF-8");
            }
            else {
                writer = new PrintWriter("traverse_log.txt", "UTF-8");
            }
            writer.print(s);
            writer.close();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public static mancalaBoard greedySearch(mancalaBoard state, int player) {
        //reverse and play as player1 if active player is 2
        int maxval = calculateEval(state);
        mancalaBoard next = new mancalaBoard(state);

        for (int index = 0; index < state.no_pits; index++) {

            mancalaBoard temp = new mancalaBoard(state);

            int stones = temp.player1[index];
            temp.has_chance = false;
            if (stones != 0) {
                //Move stones
                int p1_i = index + 1;
                int p2_i = temp.player1.length - 1;
                boolean pick = false;
                temp.player1[index] = 0;
                while (stones != 0) {
                    //add stones in player1
                    for (int i = p1_i; i < temp.no_pits && stones != 0; i++) {
                        temp.player1[i]++;
                        stones--;
                        if (stones == 0 && temp.player1[i] == 1 ) {
                            pick = true;
                            temp.player1[temp.no_pits] += temp.player1[i] + temp.player2[i + 1];
                            temp.player1[i] = 0;
                            temp.player2[i + 1] = 0;
                        }
                    }
                    if (stones == 0) {
                        temp.has_chance = false;
                    }
                    if (stones != 0) {
                        temp.player1[temp.no_pits]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = temp.player1.length - 1; i > 0 && stones != 0; i--) {
                            temp.player2[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = 0;
                    p2_i = temp.no_pits;
                }
//                temp.printBoard();
//                System.out.println("-====" + temp.getPlayer_score(1) +"-"+ temp.has_chance + "====-");
                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }
                if(temp.has_chance && !isEmpty){
                    greedySearch(temp,player);
                }
                if(player == 2){
                    if(calculateEval(temp) >= maxval && !temp.has_chance){
                        maxval = calculateEval(temp);
                        next = new mancalaBoard(temp);
                    }
                    if(calculateEval(next)>=max && !isEmpty ){
                        max = calculateEval(next);
                        opt = new mancalaBoard(next);
                    }
                }
                else {
                    if(calculateEval(temp) > maxval && !temp.has_chance){
                        maxval = calculateEval(temp);
                        next = new mancalaBoard(temp);
                    }
                    if(calculateEval(next)>max && !isEmpty ){
                        max = calculateEval(next);
                        opt = new mancalaBoard(next);
                    }
                }
            }
        }
        return next;
    }

    public static mancalaBoard reversePlayer(mancalaBoard state){
        for (int i = 0; i < (state.no_pits+1); i++) {
            int temp = state.player2[state.no_pits-i];
            state.player2[state.no_pits-i] = state.player1[i];
            state.player1[i] = temp;
        }
        return state;
    }

    public static int calculateEval(mancalaBoard b) {
        return b.getPlayer_score(1) - b.getPlayer_score(2);
    }

    public static mancalaBoard minmax(mancalaBoard state, int max_depth, int player){
        mancalaBoard next = new mancalaBoard(state);
        state.setStateName("root");
        state.depth = 0;
        state.eval = Integer.MIN_VALUE;
        traverseLog += state.getStateName()+","+state.depth+","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" : (state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval) +"\n";
        if(player == 1) {
            mancalaBoard solution = new mancalaBoard(computeMax(state, 0, max_depth, player));
            next_state += opt.printBoard();
            next_state += "\n"+opt.getPlayer_score(2);
            next_state += "\n"+opt.getPlayer_score(1);
        }
        else {
            mancalaBoard solution = new mancalaBoard(computeMax_player2(state, 0, max_depth));
            next_state += opt.printBoard();
            next_state += "\n"+opt.getPlayer_score(2);
            next_state += "\n"+opt.getPlayer_score(1);
        }
        writeToFile(next_state,1);
        writeToFile(traverseLog,2);
        return next;
    }

    private static mancalaBoard computeMax(mancalaBoard state, int depth,int max_depth, int player) {
        mancalaBoard next = new mancalaBoard(state);
        depth = depth+1;
        int maxVal = Integer.MIN_VALUE;
        for (int index = 0; index < state.no_pits; index++) {

            mancalaBoard temp = new mancalaBoard(state);
            int stones = temp.player1[index];
            temp.has_chance = false;
//            int maxVal = Integer.MIN_VALUE;
            if (stones != 0) {
                //Move stones
                int p1_i = index + 1;
                temp.player1[index] = 0;
                while (stones != 0) {
                    //add stones in player1
                    for (int i = p1_i; i < temp.no_pits && stones != 0; i++) {
                        temp.player1[i]++;
                        stones--;
                        if (stones == 0 && temp.player1[i] == 1 ) {
                            temp.player1[temp.no_pits] += temp.player1[i] + temp.player2[i + 1];
                            temp.player1[i] = 0;
                            temp.player2[i + 1] = 0;
                        }
                    }
                    if (stones != 0) {
                        temp.player1[temp.no_pits]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = temp.player1.length - 1; i > 0 && stones != 0; i--) {
                            temp.player2[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = 0;
                }

                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }

//                temp.printBoard();
                temp.setStateName("B"+(index+2));
                temp.depth = depth;
                if(!temp.has_chance && depth==max_depth) {
                    temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval(temp));//temp.eval = calculateEval(temp);
                }
                else if(depth == max_depth && temp.has_chance && isEmpty){
                    temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval(temp));//temp.eval = calculateEval(temp);
                }
                else {
                    if(temp.has_chance) temp.eval = Integer.MIN_VALUE;
//                    if(depth == max_depth) temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }


                mancalaBoard g = new mancalaBoard(temp);
                g.eval = temp.eval;
                if(temp.has_chance && !isEmpty ){
                    temp.eval = Integer.MIN_VALUE;
                    mancalaBoard subtree = computeMax(temp, depth-1, max_depth, 1);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                if(!temp.has_chance && depth<max_depth && !isEmpty){
                    mancalaBoard subtree = computeMin(temp, depth, max_depth, 1); //check with maxval
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                else if(!temp.has_chance && depth<max_depth && isEmpty){
                    g = new mancalaBoard(temp);
                    g.eval = temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }
                if(g.eval > maxVal ){
                    maxVal = g.eval;
                    state.eval = temp.eval = g.eval =maxVal;
                    next = new mancalaBoard(temp);//changed temp to g
                    next.eval = temp.eval;//changed temp to g
                    if(depth == 1 && temp.eval > max){
                        opt = temp;
                        max = temp.eval;
                    }
                    //print parent state.eval = maxVal;
                    traverseLog += state.getStateName() + "," +state.depth +","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" :(state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval)+"\n";
                    System.out.println(state.getStateName()+","+state.depth+","+ state.eval);
                }
                else{
                    traverseLog += state.getStateName() + "," +state.depth +","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" :(state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval)+"\n";
                    System.out.println(state.getStateName()+","+state.depth+","+ state.eval);
                }
            }
        }
        return next;
    }

    private static mancalaBoard computeMin(mancalaBoard state,int depth, int max_depth, int player) {
        mancalaBoard next = new mancalaBoard(state);
        int minVal = Integer.MAX_VALUE;
        depth++;
        for (int index = 1; index < state.no_pits+1; index++) {
            mancalaBoard temp = new mancalaBoard(state);
            int stones = temp.player2[index];
            temp.has_chance = false;
            temp.eval = Integer.MIN_VALUE;
            if (stones != 0) {
                //Move stones
                int p1_i = index - 1;
                temp.player2[index] = 0;
                while (stones != 0) {
                    //add stones in player2
                    for (int i = p1_i; i > 0 && stones != 0; i--) {
                        temp.player2[i]++;
                        stones--;
                        if (stones == 0 && temp.player2[i] == 1) {
                            temp.player2[0] += temp.player2[i] + temp.player1[i - 1];
                            temp.player2[i] = 0;
                            temp.player1[i - 1] = 0;
                        }
                    }
                    if (stones != 0) {
                        temp.player2[0]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = 0; i < temp.player1.length - 1 && stones != 0; i++) {
                            temp.player1[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = temp.no_pits;
                }

                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }

//                temp.printBoard();
                temp.setStateName("A"+(index+1));
                temp.depth = depth;
                if(!temp.has_chance && depth==max_depth) {
                    temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
                    System.out.println(temp.getStateName() + "," + temp.depth + "," + temp.eval);;
                }
                else if(depth == max_depth && temp.has_chance && isEmpty){
                    temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval(temp));//temp.eval = calculateEval(temp);
                }
                else {
                    if(temp.has_chance) temp.eval = Integer.MAX_VALUE;
//                    if(depth == max_depth) temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
                    System.out.println(temp.getStateName() + "," + temp.depth + "," + temp.eval);
                }

                mancalaBoard g = new mancalaBoard(temp);
                g.eval = temp.eval;
                if(temp.has_chance && !isEmpty ){
                    temp.eval = Integer.MAX_VALUE;
                    mancalaBoard subtree = computeMin(temp, depth-1, max_depth, 1);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                if(!temp.has_chance && depth < max_depth  && !isEmpty){
                    mancalaBoard subtree = computeMax(temp, depth, max_depth, 1);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                else if(!temp.has_chance && depth<max_depth && isEmpty){
                    g = new mancalaBoard(temp);
                    g.eval = temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }
                if(g.eval < minVal){
                    minVal = g.eval;
                    state.eval = temp.eval = g.eval =minVal;//while comparing with parent see if it is greater in min and smaller in max
                    next = new mancalaBoard(temp);//changed temp to g
                    next.eval = temp.eval;//changed temp to g
                    //print parent
                    traverseLog += state.getStateName() + "," +state.depth +","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" : (state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval)+"\n";
                    System.out.println(state.getStateName()+","+state.depth+","+state.eval);
                }
                else{
                    //print parent
                    traverseLog += state.getStateName() + "," +state.depth +","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" :(state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval)+"\n";
                    System.out.println(state.getStateName()+","+state.depth+","+state.eval);
                }
            }
        }
        return next;
    }

    private static mancalaBoard computeMax_player2(mancalaBoard state, int depth, int max_depth){
        mancalaBoard next = new mancalaBoard(state);
        depth = depth+1;
        int maxVal = Integer.MIN_VALUE;
        for (int index = 1; index < state.no_pits+1; index++) {

            mancalaBoard temp = new mancalaBoard(state);
            int stones = temp.player2[index];
            temp.has_chance = false;
            temp.eval = Integer.MAX_VALUE;
//            int maxVal = Integer.MIN_VALUE;
            if (stones != 0) {
                //Move stones
                int p1_i = index - 1;
                temp.player2[index] = 0;
                while (stones != 0) {
                    for (int i = p1_i; i > 0 && stones != 0; i--) {
                        temp.player2[i]++;
                        stones--;
                        if (stones == 0 && temp.player2[i] == 1 ) {
                            temp.player2[0] += temp.player2[i] + temp.player1[i - 1];
                            temp.player2[i] = 0;
                            temp.player1[i - 1] = 0;
                        }
                    }
                    if (stones != 0) {
                        temp.player2[0]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = 0; i < temp.player1.length - 1 && stones != 0; i++) {
                            temp.player1[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = temp.no_pits;
                }
//                temp.printBoard();
                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }

                temp.setStateName("A"+(index+1));
                temp.depth = depth;
                if(!temp.has_chance && depth==max_depth) {
                    temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval2(temp));//temp.eval = calculateEval(temp);
                }
                else if(depth == max_depth && temp.has_chance && isEmpty){
                    temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval2(temp));//temp.eval = calculateEval(temp);
                }
                else {
                    if(temp.has_chance) temp.eval = Integer.MIN_VALUE;
//                    if(depth == max_depth) temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }

                mancalaBoard g = new mancalaBoard(temp);
                g.eval = temp.eval;
                if(temp.has_chance && !isEmpty ){
                    temp.eval = Integer.MIN_VALUE;
                    mancalaBoard subtree = computeMax_player2(temp, depth - 1, max_depth);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }

                if(!temp.has_chance && depth<max_depth && !isEmpty){
                    mancalaBoard subtree = computeMin_player2(temp, depth, max_depth); //check with maxval
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                else if(!temp.has_chance && depth<max_depth && isEmpty){
                    g = new mancalaBoard(temp);
                    g.eval = temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }
                if(g.eval > maxVal ){
                    maxVal = g.eval;
                    state.eval = temp.eval = g.eval =maxVal;
                    next = new mancalaBoard(temp);
                    next.eval = temp.eval;
                    if(depth == 1 && temp.eval > max){
                        opt = temp;
                        max = temp.eval;
                    }
                    //print parent state.eval = maxVal;
                    traverseLog += state.getStateName() + "," +state.depth +","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" :(state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+ state.eval);
                }
                else{
                    traverseLog += state.getStateName() + "," +state.depth +","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" :(state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+ state.eval);
                }
            }
        }
        return next;
    }

    private static mancalaBoard computeMin_player2(mancalaBoard state, int depth, int max_depth) {
        mancalaBoard next = new mancalaBoard(state);
        int minVal = Integer.MAX_VALUE;
        depth++;
        for (int index = 0; index < state.no_pits; index++) {

            mancalaBoard temp = new mancalaBoard(state);
            int stones = temp.player1[index];
            temp.has_chance = false;
            temp.eval = Integer.MIN_VALUE;
//            int maxVal = Integer.MIN_VALUE;
            if (stones != 0) {
                //Move stones
                int p1_i = index + 1;
                temp.player1[index] = 0;
                while (stones != 0) {
                    //add stones in player1
                    for (int i = p1_i; i < temp.no_pits && stones != 0; i++) {
                        temp.player1[i]++;
                        stones--;
                        if (stones == 0 && temp.player1[i] == 1 ) {
                            temp.player1[temp.no_pits] += temp.player1[i] + temp.player2[i + 1];
                            temp.player1[i] = 0;
                            temp.player2[i + 1] = 0;
                        }
                    }
                    if (stones != 0) {
                        temp.player1[temp.no_pits]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = temp.player1.length - 1; i > 0 && stones != 0; i--) {
                            temp.player2[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = 0;
                }
//                temp.printBoard();
                temp.setStateName("B"+(index+2));
                temp.depth = depth;
                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }
                if(!temp.has_chance && depth==max_depth) {
                    temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + temp.eval);;
                }
                else if(depth == max_depth && temp.has_chance && isEmpty){
                    temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval2(temp));//temp.eval = calculateEval(temp);
                }
                else {
                    if(temp.has_chance) temp.eval = Integer.MAX_VALUE;
//                    if(depth == max_depth) temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + temp.eval);
                }

                mancalaBoard g = new mancalaBoard(temp);
                g.eval = temp.eval;
                if(temp.has_chance && !isEmpty ){
                    temp.eval = Integer.MAX_VALUE;
                    mancalaBoard subtree = computeMin_player2(temp, depth - 1, max_depth);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                if(!temp.has_chance && depth < max_depth && !isEmpty){
                    mancalaBoard subtree = computeMax_player2(temp, depth, max_depth);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                else if(!temp.has_chance && depth<max_depth && isEmpty){
                    g = new mancalaBoard(temp);
                    g.eval = temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }
                if(g.eval < minVal){
                    minVal = g.eval;
                    state.eval = temp.eval = g.eval =minVal;//while comparing with parent see if it is greater in min and smaller in max
                    next = new mancalaBoard(temp);
                    next.eval = temp.eval;
                    //print parent
                    traverseLog += state.getStateName() + "," +state.depth +","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" : (state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+state.eval);
                }
                else{
                    //print parent
                    traverseLog += state.getStateName() + "," +state.depth +","+ ((state.eval == Integer.MAX_VALUE)? "Infinity" :(state.eval == Integer.MIN_VALUE)? "-Infinity" : state.eval)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+state.eval);
                }
            }
        }
        return next;
    }

    private static int calculateEval2(mancalaBoard b) {
        return b.getPlayer_score(2) - b.getPlayer_score(1);
    }

    private static mancalaBoard alphaBetaPruning(mancalaBoard state, int cut_off_level, int active_player) {
        traverseLog = "Node,Depth,Value,Alpha,Beta\n";
        state.setStateName("root");
        state.depth = 0;
        state.eval = Integer.MIN_VALUE;
        state.ALPHA = Integer.MIN_VALUE;//-Infinity
        state.BETA = Integer.MAX_VALUE;//Infinity
        traverseLog += state.getStateName()+","+state.depth+","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
        if(active_player == 1) {
            mancalaBoard solution = new mancalaBoard(Max(state,0,cut_off_level,active_player));
            next_state += opt.printBoard();
            next_state += "\n"+opt.getPlayer_score(2);
            next_state += "\n"+opt.getPlayer_score(1);
        }
        else {
            mancalaBoard solution = new mancalaBoard(Max2(state, 0, cut_off_level));
            next_state += opt.printBoard();
            next_state += "\n"+opt.getPlayer_score(2);
            next_state += "\n"+opt.getPlayer_score(1);
        }
        writeToFile(next_state,1);
        writeToFile(traverseLog,2);
        return state;
    }

    private static mancalaBoard Max(mancalaBoard state, int depth, int max_depth, int player){
        mancalaBoard next = new mancalaBoard(state);
        depth = depth+1;
        int maxVal = Integer.MIN_VALUE;
        for (int index = 0; index < state.no_pits; index++) {

            mancalaBoard temp = new mancalaBoard(state);
            int stones = temp.player1[index];
            temp.has_chance = false;
            temp.ALPHA = state.ALPHA;//
            temp.BETA =state.BETA;//
//            int maxVal = Integer.MIN_VALUE;
            if (stones != 0) {
                //Move stones
                int p1_i = index + 1;
                temp.player1[index] = 0;
                while (stones != 0) {
                    //add stones in player1
                    for (int i = p1_i; i < temp.no_pits && stones != 0; i++) {
                        temp.player1[i]++;
                        stones--;
                        if (stones == 0 && temp.player1[i] == 1 ) {
                            temp.player1[temp.no_pits] += temp.player1[i] + temp.player2[i + 1];
                            temp.player1[i] = 0;
                            temp.player2[i + 1] = 0;
                        }
                    }
                    if (stones != 0) {
                        temp.player1[temp.no_pits]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = temp.player1.length - 1; i > 0 && stones != 0; i--) {
                            temp.player2[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = 0;
                }
                temp.setStateName("B"+(index+2));
                temp.depth = depth;
                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }
                if(!temp.has_chance && depth==max_depth) {
                    temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval(temp)+"," + formatValue(temp.ALPHA)+"," + formatValue(temp.BETA));
                }
                else if(depth == max_depth && temp.has_chance && isEmpty){
                    temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval(temp));//temp.eval = calculateEval(temp);
                }
                else {
                    if(temp.has_chance) temp.eval = Integer.MIN_VALUE;
//                    if(depth == max_depth) temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA));
                }

                mancalaBoard g = new mancalaBoard(temp);
                g.eval = temp.eval;
                if(temp.has_chance && !isEmpty ){
                    temp.eval = Integer.MIN_VALUE;
                    mancalaBoard subtree = Max(temp, depth - 1, max_depth, 1);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                if(!temp.has_chance && depth<max_depth && !isEmpty){
                    mancalaBoard subtree = Min(temp, depth, max_depth, 1); //check with maxval
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                else if(!temp.has_chance && depth<max_depth && isEmpty){
                    g = new mancalaBoard(temp);
                    g.eval = temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }
                if(g.eval > maxVal ){
                    maxVal = g.eval;
                    state.eval = temp.eval = g.eval =maxVal;
                    next = new mancalaBoard(temp);
                    next.eval = temp.eval;
                    if(depth == 1 && temp.eval > max){
                        opt = temp;
                        max = temp.eval;
                    }

                    if(state.BETA <= g.eval){
                        traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
                        return next;
                    }

                    if(g.eval > state.ALPHA){
                        state.ALPHA =g.eval;
                    }
                    //print parent state.eval = maxVal;
                    traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA));

                }
                else{
                    traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA));
                }
            }
        }
        return next;
    }

    private static mancalaBoard Min(mancalaBoard state,int depth, int max_depth, int player) {
        mancalaBoard next = new mancalaBoard(state);
        int minVal = Integer.MAX_VALUE;
        depth++;
        for (int index = 1; index < state.no_pits+1; index++) {
            mancalaBoard temp = new mancalaBoard(state);
            int stones = temp.player2[index];
            temp.has_chance = false;
            temp.ALPHA = state.ALPHA;//
            temp.BETA =state.BETA;//
            temp.eval = Integer.MIN_VALUE;
            if (stones != 0) {
                //Move stones
                int p1_i = index - 1;
                temp.player2[index] = 0;
                while (stones != 0) {
                    //add stones in player2
                    for (int i = p1_i; i > 0 && stones != 0; i--) {
                        temp.player2[i]++;
                        stones--;
                        if (stones == 0 && temp.player2[i] == 1 ) {
                            temp.player2[0] += temp.player2[i] + temp.player1[i - 1];
                            temp.player2[i] = 0;
                            temp.player1[i - 1] = 0;
                        }
                    }
                    if (stones != 0) {
                        temp.player2[0]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = 0; i < temp.player1.length - 1 && stones != 0; i++) {
                            temp.player1[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = temp.no_pits;
                }
//                temp.printBoard();
                temp.setStateName("A"+(index+1));
                temp.depth = depth;
                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }
                if(!temp.has_chance && depth==max_depth) {
                    temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA));
                }
                else if(depth == max_depth && temp.has_chance && isEmpty){
                    temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval(temp));//temp.eval = calculateEval(temp);
                }
                else {
                    if(temp.has_chance) temp.eval = Integer.MAX_VALUE;
//                    if(depth == max_depth) temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA));
                }

                mancalaBoard g = new mancalaBoard(temp);
                g.eval = temp.eval;
                if(temp.has_chance && !isEmpty ){
                    temp.eval = Integer.MAX_VALUE;
                    mancalaBoard subtree = Min(temp, depth - 1, max_depth, 1);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                if(!temp.has_chance && depth < max_depth && !isEmpty){
                    mancalaBoard subtree = Max(temp, depth, max_depth, 1);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                else if(!temp.has_chance && depth<max_depth && isEmpty){
                    g = new mancalaBoard(temp);
                    g.eval = temp.eval = calculateEval(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }
                if(g.eval < minVal){
                    minVal = g.eval;
                    state.eval = temp.eval = g.eval =minVal;//while comparing with parent see if it is greater in min and smaller in max
                    next = new mancalaBoard(temp);
                    next.eval = temp.eval;
                    //Beta <= Alpha
                    if(g.eval <= state.ALPHA){
                        traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
                        return next;
                    }

                    if(g.eval < state.BETA){
                        state.BETA = g.eval;
                    }

                    //print parent
                    traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA));

                    //pruning condition


                }
                else{
                    //print parent
                    traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA));
                }
            }
        }
        return next;
    }

    private static mancalaBoard Max2(mancalaBoard state, int depth, int max_depth){
        mancalaBoard next = new mancalaBoard(state);
        depth = depth+1;
        int maxVal = Integer.MIN_VALUE;
        for (int index = 1; index < state.no_pits+1; index++) {

            mancalaBoard temp = new mancalaBoard(state);
            int stones = temp.player2[index];
            temp.has_chance = false;
            temp.eval = Integer.MAX_VALUE;
            temp.ALPHA = state.ALPHA;//
            temp.BETA =state.BETA;
//            int maxVal = Integer.MIN_VALUE;
            if (stones != 0) {
                //Move stones
                int p1_i = index - 1;
                temp.player2[index] = 0;
                while (stones != 0) {
                    for (int i = p1_i; i > 0 && stones != 0; i--) {
                        temp.player2[i]++;
                        stones--;
                        if (stones == 0 && temp.player2[i] == 1 ) {
                            temp.player2[0] += temp.player2[i] + temp.player1[i - 1];
                            temp.player2[i] = 0;
                            temp.player1[i - 1] = 0;
                        }
                    }
                    if (stones != 0) {
                        temp.player2[0]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = 0; i < temp.player1.length - 1 && stones != 0; i++) {
                            temp.player1[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = temp.no_pits;
                }
//                temp.printBoard();

                temp.setStateName("A"+(index+1));
                temp.depth = depth;
                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }
                if(!temp.has_chance && depth==max_depth) {
                    temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval2(temp));//temp.eval = calculateEval(temp);
                }
                else if(depth == max_depth && temp.has_chance && isEmpty){
                    temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval2(temp));//temp.eval = calculateEval(temp);
                }
                else {
                    if(temp.has_chance) temp.eval = Integer.MIN_VALUE;
//                    if(depth == max_depth) temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }

                mancalaBoard g = new mancalaBoard(temp);
                g.eval = temp.eval;
                if(temp.has_chance && !isEmpty ){
                    temp.eval = Integer.MIN_VALUE;
                    mancalaBoard subtree = Max2(temp, depth - 1, max_depth);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                if(!temp.has_chance && depth<max_depth && !isEmpty){
                    mancalaBoard subtree = Min2(temp, depth, max_depth); //check with maxval
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                else if(!temp.has_chance && depth<max_depth && isEmpty){
                    g = new mancalaBoard(temp);
                    g.eval = temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }
                if(g.eval > maxVal ){
                    maxVal = g.eval;
                    state.eval = temp.eval = g.eval =maxVal;
                    next = new mancalaBoard(temp);
                    next.eval = temp.eval;
                    if(depth == 1 && temp.eval > max){
                        opt = temp;
                        max = temp.eval;
                    }

                    if(state.BETA <= g.eval){
                        traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
                        return next;
                    }

                    if(g.eval > state.ALPHA){
                        state.ALPHA =g.eval;
                    }

                    //print parent state.eval = maxVal;
                    traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+ state.eval);
                }
                else{
                    traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+ state.eval);
                }
            }
        }
        return next;
    }

    private static mancalaBoard Min2(mancalaBoard state, int depth, int max_depth) {
        mancalaBoard next = new mancalaBoard(state);
        int minVal = Integer.MAX_VALUE;
        depth++;
        for (int index = 0; index < state.no_pits; index++) {

            mancalaBoard temp = new mancalaBoard(state);
            int stones = temp.player1[index];
            temp.has_chance = false;
            temp.ALPHA = state.ALPHA;//
            temp.BETA =state.BETA;
            temp.eval = Integer.MIN_VALUE;
//            int maxVal = Integer.MIN_VALUE;
            if (stones != 0) {
                //Move stones
                int p1_i = index + 1;
                temp.player1[index] = 0;
                while (stones != 0) {
                    //add stones in player1
                    for (int i = p1_i; i < temp.no_pits && stones != 0; i++) {
                        temp.player1[i]++;
                        stones--;
                        if (stones == 0 && temp.player1[i] == 1 ) {
                            temp.player1[temp.no_pits] += temp.player1[i] + temp.player2[i + 1];
                            temp.player1[i] = 0;
                            temp.player2[i + 1] = 0;
                        }
                    }
                    if (stones != 0) {
                        temp.player1[temp.no_pits]++;
                        stones--;
                        if(stones==0) temp.has_chance = true;
                    }
                    if (stones > 0) {
                        for (int i = temp.player1.length - 1; i > 0 && stones != 0; i--) {
                            temp.player2[i]++;
                            stones--;
                        }
                    }
                    //reset index
                    p1_i = 0;
                }
//                temp.printBoard();
                temp.setStateName("B"+(index+2));
                temp.depth = depth;
                boolean isEmpty = false;
                boolean isP1E = true;
                boolean isP2E = true;
                for (int i = 0; i < temp.no_pits; i++) {
                    if (temp.player1[i] != 0) {
                        isP1E = false;
                        break;
                    }
                }
                for (int i = 1; i < temp.no_pits+1; i++) {
                    if (temp.player2[i] != 0) {
                        isP2E = false;
                        break;
                    }
                }
                if(isP1E){
                    for (int i = 1; i <= temp.no_pits; i++) {
                        temp.player2[0] += temp.player2[i];
                        temp.player2[i] = 0;
                    }
                    isEmpty = true;
                }
                if(isP2E){
                    for (int i = 0; i < temp.no_pits; i++) {
                        temp.player1[temp.no_pits] += temp.player1[i];
                        temp.player1[i] = 0;
                    }
                    isEmpty = true;
                }
                if(!temp.has_chance && depth==max_depth) {
                    temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + temp.eval);;
                }
                else if(depth == max_depth && temp.has_chance && isEmpty){
                    temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + calculateEval(temp));//temp.eval = calculateEval(temp);
                }
                else {
                    if(temp.has_chance) temp.eval = Integer.MAX_VALUE;
//                    if(depth == max_depth) temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ formatValue(temp.eval)+","+formatValue(temp.ALPHA)+","+formatValue(temp.BETA)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + temp.eval);
                }

                mancalaBoard g = new mancalaBoard(temp);
                g.eval = temp.eval;
                if(temp.has_chance && !isEmpty) {
                    temp.eval = Integer.MAX_VALUE;
                    mancalaBoard subtree = Min2(temp, depth - 1, max_depth);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                if(!temp.has_chance && depth < max_depth && !isEmpty){
                    mancalaBoard subtree = Max2(temp, depth, max_depth);
                    g = new mancalaBoard(subtree);
                    g.eval = subtree.eval;
                }
                else if(!temp.has_chance && depth<max_depth && isEmpty){
                    g = new mancalaBoard(temp);
                    g.eval = temp.eval = calculateEval2(temp);
                    traverseLog += temp.getStateName() + "," +temp.depth +","+ ((temp.eval == Integer.MAX_VALUE)? "Infinity" :(temp.eval == Integer.MIN_VALUE)? "-Infinity" : temp.eval)+"\n";
//                    System.out.println(temp.getStateName() + "," + temp.depth + "," + ((temp.eval == Integer.MAX_VALUE)? "Infinity" : temp.eval));
                }
                if(g.eval < minVal){
                    minVal = g.eval;
                    state.eval = temp.eval = g.eval =minVal;//while comparing with parent see if it is greater in min and smaller in max
                    next = new mancalaBoard(temp);
                    next.eval = temp.eval;

                    if(g.eval <= state.ALPHA){
                        traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
                        return next;
                    }

                    if(g.eval < state.BETA){
                        state.BETA = g.eval;
                    }

                    //print parent
                    traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+state.eval);
                }
                else{
                    //print parent
                    traverseLog += state.getStateName() + "," +state.depth +","+ formatValue(state.eval)+","+formatValue(state.ALPHA)+","+formatValue(state.BETA)+"\n";
//                    System.out.println(state.getStateName()+","+state.depth+","+state.eval);
                }
            }
        }
        return next;
    }

    private static String formatValue(int s){
        return ((s == Integer.MAX_VALUE)? "Infinity" :(s == Integer.MIN_VALUE)? "-Infinity" : ((Integer)s).toString());
    }

}

class mancalaBoard implements Cloneable {
    int no_pits;
    int player1[] ;
    int player2[] ;
    boolean has_chance = false;
    String stateName = "";
    int depth;
    int eval = Integer.MAX_VALUE;
    int ALPHA;
    int BETA;

    public mancalaBoard(int n, int p1[], int p2[], int p1_m, int p2_m) {
        no_pits = n;
        player1 = new int[n+1];
        player2 = new int[n+1];
        player1[n] = p1_m;
        player2[0] = p2_m;
        player1 = p1.clone();
        player2 = p2.clone();
        has_chance = false;
    }
    public void setStateName(String n){
        stateName = n;
    }
    public String getStateName(){
        return stateName;
    }
    public mancalaBoard(){
    }
    public mancalaBoard(mancalaBoard b) {
        this.player1 = b.player1.clone();
        this.player2 = b.player2.clone();
        this.no_pits = b.no_pits;
        this.has_chance = b.has_chance;
    }
    public String printBoard() {
        String p1 = "";
        String p2 = "";
//        for (int i = 0; i < no_pits+1; i++) {
//            p1 += player1[i] + " ";
//            p2 += player2[i] + " ";
//        }
        for (int i = 0; i < no_pits; i++) {
            p1 += player1[i] + " ";
        }
        for (int i = 1; i < no_pits+1; i++) {
            p2 += player2[i] + " ";
        }
//        System.out.println(p2 + "\n" + p1);
        return p2 + "\n" + p1;
    }
    public int getPlayer_score(int player){
        if(player == 1)
            return player1[no_pits];
        if(player == 2)
            return player2[0];
        return 0;
    }
}