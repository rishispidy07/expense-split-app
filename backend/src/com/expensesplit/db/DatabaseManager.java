package com.expensesplit.db;

import com.expensesplit.config.Config;
import com.expensesplit.model.*;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static Connection connection;

    public static synchronized void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);
            connection.setAutoCommit(true);
            System.out.println("[DB] Connected to MySQL database");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL JDBC Driver not found. Add mysql-connector-j.jar to lib/");
            System.err.println("[DB] Download from: https://mvnrepository.com/artifact/com.mysql/mysql-connector-j");
            throw new RuntimeException("MySQL driver not found", e);
        } catch (SQLException e) {
            System.err.println("[DB] Failed to connect to database: " + e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initialize();
            }
        } catch (SQLException e) {
            initialize();
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static int createUser(String username, String email, String passwordHash, String fullName, String phone) {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, phone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, fullName);
            ps.setString(5, phone);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static User findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static User findUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) users.add(mapUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public static boolean updateUserBalance(int userId, double amount) {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Group operations
    public static int createGroup(String name, String description, int createdBy) {
        String sql = "INSERT INTO expense_groups (name, description, created_by) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, createdBy);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int groupId = rs.next() ? rs.getInt(1) : -1;
            if (groupId > 0) addGroupMember(groupId, createdBy, "admin");
            return groupId;
        } catch (SQLException e) { e.printStackTrace(); return -1; }
    }

    public static boolean updateGroup(int groupId, String name, String description) {
        String sql = "UPDATE expense_groups SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, groupId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean addGroupMember(int groupId, int userId, String role) {
        String sql = "INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE role = VALUES(role)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static List<Map<String, Object>> getUserGroups(int userId) {
        List<Map<String, Object>> groups = new ArrayList<>();
        String sql = "SELECT g.*, gm.role FROM expense_groups g JOIN group_members gm ON g.id = gm.group_id WHERE gm.user_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> group = new LinkedHashMap<>();
                group.put("id", rs.getInt("id"));
                group.put("name", rs.getString("name"));
                group.put("description", rs.getString("description"));
                group.put("walletBalance", rs.getDouble("wallet_balance"));
                group.put("role", rs.getString("role"));
                group.put("createdBy", rs.getInt("created_by"));
                groups.add(group);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return groups;
    }

    public static List<Map<String, Object>> getGroupMembers(int groupId) {
        List<Map<String, Object>> members = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.full_name, u.email, gm.role FROM users u JOIN group_members gm ON u.id = gm.user_id WHERE gm.group_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> member = new LinkedHashMap<>();
                member.put("id", rs.getInt("id"));
                member.put("username", rs.getString("username"));
                member.put("fullName", rs.getString("full_name"));
                member.put("email", rs.getString("email"));
                member.put("role", rs.getString("role"));
                members.add(member);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return members;
    }

    public static boolean isGroupMember(int groupId, int userId) {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean removeGroupMember(int groupId, int userId) {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Expense operations
    public static int createExpense(Integer groupId, int paidBy, String description, double amount, String category, String splitType, int year, int month) {
        String sql = "INSERT INTO expenses (group_id, paid_by, description, amount, category, split_type, expense_year, expense_month) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (groupId != null) ps.setInt(1, groupId); else ps.setNull(1, Types.INTEGER);
            ps.setInt(2, paidBy);
            ps.setString(3, description);
            ps.setDouble(4, amount);
            ps.setString(5, category);
            ps.setString(6, splitType);
            ps.setInt(7, year);
            ps.setInt(8, month);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) { e.printStackTrace(); return -1; }
    }

    public static boolean createExpenseSplits(int expenseId, int[] userIds, double[] amounts) {
        String sql = "INSERT INTO expense_splits (expense_id, user_id, amount) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < userIds.length; i++) {
                ps.setInt(1, expenseId);
                ps.setInt(2, userIds[i]);
                ps.setDouble(3, amounts[i]);
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean deleteExpense(int expenseId, int userId) {
        String sql = "DELETE FROM expenses WHERE id = ? AND paid_by = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static List<Map<String, Object>> getGroupExpenses(int groupId) {
        List<Map<String, Object>> expenses = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name as paid_by_name FROM expenses e JOIN users u ON e.paid_by = u.id WHERE e.group_id = ? ORDER BY e.created_at DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> expense = new LinkedHashMap<>();
                expense.put("id", rs.getInt("id"));
                expense.put("description", rs.getString("description"));
                expense.put("amount", rs.getDouble("amount"));
                expense.put("category", rs.getString("category"));
                expense.put("splitType", rs.getString("split_type"));
                expense.put("paidBy", rs.getInt("paid_by"));
                expense.put("paidByName", rs.getString("paid_by_name"));
                expense.put("createdAt", rs.getTimestamp("created_at").toString());
                expenses.add(expense);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return expenses;
    }

    public static Map<String, Object> getGroupSplits(int groupId) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> members = new ArrayList<>();
            String mSql = "SELECT u.id, u.full_name, u.username FROM users u JOIN group_members gm ON u.id = gm.user_id WHERE gm.group_id = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(mSql)) {
                ps.setInt(1, groupId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getInt("id"));
                    m.put("fullName", rs.getString("full_name"));
                    m.put("username", rs.getString("username"));
                    members.add(m);
                }
            }

            Map<Integer, Double> totalPaid = new LinkedHashMap<>();
            Map<Integer, Double> totalShare = new LinkedHashMap<>();
            List<Map<String, Object>> expenseDetails = new ArrayList<>();

            String eSql = "SELECT e.id, e.description, e.amount, e.paid_by, u.full_name as paid_by_name, e.category, e.created_at FROM expenses e JOIN users u ON e.paid_by = u.id WHERE e.group_id = ? ORDER BY e.created_at DESC";
            try (PreparedStatement ps = getConnection().prepareStatement(eSql)) {
                ps.setInt(1, groupId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int expId = rs.getInt("id");
                    double amount = rs.getDouble("amount");
                    int paidBy = rs.getInt("paid_by");
                    String paidByName = rs.getString("paid_by_name");
                    String desc = rs.getString("description");
                    String cat = rs.getString("category");
                    String createdAt = rs.getTimestamp("created_at").toString();

                    totalPaid.merge(paidBy, amount, Double::sum);

                    String sSql = "SELECT user_id, amount FROM expense_splits WHERE expense_id = ?";
                    try (PreparedStatement ps2 = getConnection().prepareStatement(sSql)) {
                        ps2.setInt(1, expId);
                        ResultSet rs2 = ps2.executeQuery();
                        List<Map<String, Object>> splits = new ArrayList<>();
                        while (rs2.next()) {
                            int userId = rs2.getInt("user_id");
                            double shareAmount = rs2.getDouble("amount");
                            totalShare.merge(userId, shareAmount, Double::sum);
                            Map<String, Object> split = new LinkedHashMap<>();
                            split.put("userId", userId);
                            split.put("amount", shareAmount);
                            splits.add(split);
                        }
                        Map<String, Object> exp = new LinkedHashMap<>();
                        exp.put("id", expId);
                        exp.put("description", desc);
                        exp.put("amount", amount);
                        exp.put("category", cat);
                        exp.put("paidBy", paidBy);
                        exp.put("paidByName", paidByName);
                        exp.put("createdAt", createdAt);
                        exp.put("splits", splits);
                        expenseDetails.add(exp);
                    }
                }
            }

            List<Map<String, Object>> personSummaries = new ArrayList<>();
            for (Map<String, Object> member : members) {
                int uid = (int) member.get("id");
                double paid = totalPaid.getOrDefault(uid, 0.0);
                double share = totalShare.getOrDefault(uid, 0.0);
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("id", uid);
                summary.put("fullName", member.get("fullName"));
                summary.put("username", member.get("username"));
                summary.put("totalPaid", paid);
                summary.put("totalShare", share);
                summary.put("netBalance", paid - share);
                personSummaries.add(summary);
            }

            double groupTotal = totalPaid.values().stream().mapToDouble(Double::doubleValue).sum();
            int memberCount = members.size();
            double perPerson = memberCount > 0 ? groupTotal / memberCount : 0;

            List<Map<String, Object>> settlements = new ArrayList<>();
            List<Map<String, Object>> debtors = new ArrayList<>();
            List<Map<String, Object>> creditors = new ArrayList<>();
            for (Map<String, Object> ps : personSummaries) {
                double net = (double) ps.get("netBalance");
                if (net < -0.01) {
                    Map<String, Object> d = new LinkedHashMap<>(ps);
                    d.put("amount", Math.abs(net));
                    debtors.add(d);
                } else if (net > 0.01) {
                    Map<String, Object> c = new LinkedHashMap<>(ps);
                    c.put("amount", net);
                    creditors.add(c);
                }
            }
            debtors.sort((a, b) -> Double.compare((double) b.get("amount"), (double) a.get("amount")));
            creditors.sort((a, b) -> Double.compare((double) b.get("amount"), (double) a.get("amount")));

            int di = 0, ci = 0;
            while (di < debtors.size() && ci < creditors.size()) {
                double debtAmt = (double) debtors.get(di).get("amount");
                double creditAmt = (double) creditors.get(ci).get("amount");
                double settleAmt = Math.min(debtAmt, creditAmt);
                if (settleAmt > 0.01) {
                    Map<String, Object> st = new LinkedHashMap<>();
                    st.put("fromId", debtors.get(di).get("id"));
                    st.put("fromName", debtors.get(di).get("fullName"));
                    st.put("toId", creditors.get(ci).get("id"));
                    st.put("toName", creditors.get(ci).get("fullName"));
                    st.put("amount", Math.round(settleAmt * 100.0) / 100.0);
                    settlements.add(st);
                }
                debtors.get(di).put("amount", Math.round((debtAmt - settleAmt) * 100.0) / 100.0);
                creditors.get(ci).put("amount", Math.round((creditAmt - settleAmt) * 100.0) / 100.0);
                if ((double) debtors.get(di).get("amount") < 0.01) di++;
                if ((double) creditors.get(ci).get("amount") < 0.01) ci++;
            }

            result.put("members", personSummaries);
            result.put("expenses", expenseDetails);
            result.put("groupTotal", Math.round(groupTotal * 100.0) / 100.0);
            result.put("perPerson", Math.round(perPerson * 100.0) / 100.0);
            result.put("settlements", settlements);
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public static List<Map<String, Object>> getUserExpenses(int userId) {
        List<Map<String, Object>> expenses = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name as paid_by_name, g.name as group_name FROM expenses e JOIN users u ON e.paid_by = u.id LEFT JOIN expense_groups g ON e.group_id = g.id WHERE e.paid_by = ? OR e.id IN (SELECT expense_id FROM expense_splits WHERE user_id = ?) ORDER BY e.created_at DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> expense = new LinkedHashMap<>();
                expense.put("id", rs.getInt("id"));
                expense.put("description", rs.getString("description"));
                expense.put("amount", rs.getDouble("amount"));
                expense.put("category", rs.getString("category"));
                expense.put("splitType", rs.getString("split_type"));
                expense.put("paidBy", rs.getInt("paid_by"));
                expense.put("paidByName", rs.getString("paid_by_name"));
                expense.put("groupName", rs.getString("group_name"));
                expense.put("expenseYear", rs.getInt("expense_year"));
                expense.put("expenseMonth", rs.getInt("expense_month"));
                expense.put("createdAt", rs.getTimestamp("created_at").toString());
                expenses.add(expense);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return expenses;
    }

    // Settlement operations
    public static int createSettlement(int fromUserId, int toUserId, double amount, Integer groupId) {
        String sql = "INSERT INTO settlements (from_user_id, to_user_id, amount, group_id, status) VALUES (?, ?, ?, ?, 'pending')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fromUserId);
            ps.setInt(2, toUserId);
            ps.setDouble(3, amount);
            if (groupId != null) ps.setInt(4, groupId); else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) { e.printStackTrace(); return -1; }
    }

    public static List<Map<String, Object>> getGroupBalances(int groupId) {
        List<Map<String, Object>> balances = new ArrayList<>();
        String sql = "SELECT u.id, u.full_name, u.username, " +
            "COALESCE(SUM(CASE WHEN s.to_user_id = u.id AND s.status = 'pending' THEN s.amount ELSE 0 END), 0) as owed_to_me, " +
            "COALESCE(SUM(CASE WHEN s.from_user_id = u.id AND s.status = 'pending' THEN s.amount ELSE 0 END), 0) as i_owe " +
            "FROM users u JOIN group_members gm ON u.id = gm.user_id " +
            "LEFT JOIN settlements s ON (s.from_user_id = u.id OR s.to_user_id = u.id) AND s.group_id = ? " +
            "WHERE gm.group_id = ? GROUP BY u.id, u.full_name, u.username";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> b = new LinkedHashMap<>();
                b.put("id", rs.getInt("id"));
                b.put("fullName", rs.getString("full_name"));
                b.put("username", rs.getString("username"));
                b.put("owedToMe", rs.getDouble("owed_to_me"));
                b.put("iOwe", rs.getDouble("i_owe"));
                b.put("netBalance", rs.getDouble("owed_to_me") - rs.getDouble("i_owe"));
                balances.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return balances;
    }

    public static boolean completeSettlement(int settlementId) {
        String sql = "UPDATE settlements SET status = 'completed', completed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, settlementId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static List<Map<String, Object>> getUserSettlements(int userId) {
        List<Map<String, Object>> settlements = new ArrayList<>();
        String sql = "SELECT s.*, u1.full_name as from_name, u2.full_name as to_name FROM settlements s JOIN users u1 ON s.from_user_id = u1.id JOIN users u2 ON s.to_user_id = u2.id WHERE s.from_user_id = ? OR s.to_user_id = ? ORDER BY s.created_at DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> settlement = new LinkedHashMap<>();
                settlement.put("id", rs.getInt("id"));
                settlement.put("fromUserId", rs.getInt("from_user_id"));
                settlement.put("toUserId", rs.getInt("to_user_id"));
                settlement.put("fromUserName", rs.getString("from_name"));
                settlement.put("toUserName", rs.getString("to_name"));
                settlement.put("amount", rs.getDouble("amount"));
                settlement.put("status", rs.getString("status"));
                settlement.put("createdAt", rs.getTimestamp("created_at").toString());
                settlements.add(settlement);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return settlements;
    }

    // Wallet operations
    public static boolean updateGroupWallet(int groupId, double amount) {
        String sql = "UPDATE expense_groups SET wallet_balance = wallet_balance + ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, groupId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean addWalletTransaction(int groupId, int userId, String type, double amount, String description) {
        String sql = "INSERT INTO wallet_transactions (group_id, user_id, type, amount, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (groupId > 0) ps.setInt(1, groupId); else ps.setNull(1, Types.INTEGER);
            ps.setInt(2, userId);
            ps.setString(3, type);
            ps.setDouble(4, amount);
            ps.setString(5, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static List<Map<String, Object>> getWalletTransactions(int groupId) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        String sql = "SELECT wt.*, u.full_name as user_name FROM wallet_transactions wt JOIN users u ON wt.user_id = u.id WHERE wt.group_id = ? ORDER BY wt.created_at DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> txn = new LinkedHashMap<>();
                txn.put("id", rs.getInt("id"));
                txn.put("userName", rs.getString("user_name"));
                txn.put("type", rs.getString("type"));
                txn.put("amount", rs.getDouble("amount"));
                txn.put("description", rs.getString("description"));
                txn.put("createdAt", rs.getTimestamp("created_at").toString());
                transactions.add(txn);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return transactions;
    }

    public static Group findGroupById(int groupId) {
        String sql = "SELECT * FROM expense_groups WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Group(rs.getInt("id"), rs.getString("name"), rs.getString("description"),
                    rs.getInt("created_by"), rs.getDouble("wallet_balance"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static List<Map<String, Object>> getCategorySummary(int userId, int year, int month) {
        String sql;
        List<Map<String, Object>> list = new ArrayList<>();
        if (month == 0) {
            sql = "SELECT category, SUM(amount) as total FROM expenses WHERE paid_by = ? AND expense_year = ? GROUP BY category ORDER BY total DESC";
        } else {
            sql = "SELECT category, SUM(amount) as total FROM expenses WHERE paid_by = ? AND expense_year = ? AND expense_month = ? GROUP BY category ORDER BY total DESC";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, year);
            if (month > 0) ps.setInt(3, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("category", rs.getString("category"));
                m.put("total", rs.getDouble("total"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<Map<String, Object>> getMonthlySummary(int userId, int year) {
        String sql = "SELECT expense_month as month, SUM(amount) as total FROM expenses WHERE paid_by = ? AND expense_year = ? GROUP BY expense_month ORDER BY expense_month";
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("month", rs.getInt("month"));
                m.put("total", rs.getDouble("total"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static Map<String, Object> getMonthlyTotals(int userId, int year, int month) {
        String sql;
        if (month == 0) {
            sql = "SELECT COUNT(*) as count, COALESCE(SUM(amount),0) as total FROM expenses WHERE paid_by = ? AND expense_year = ?";
        } else {
            sql = "SELECT COUNT(*) as count, COALESCE(SUM(amount),0) as total FROM expenses WHERE paid_by = ? AND expense_year = ? AND expense_month = ?";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, year);
            if (month > 0) ps.setInt(3, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("count", rs.getInt("count"));
                m.put("total", rs.getDouble("total"));
                return m;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Map.of("count", 0, "total", 0.0);
    }

    public static List<Map<String, Object>> getGroupSummary(int userId, int year, int month) {
        String sql;
        List<Map<String, Object>> list = new ArrayList<>();
        if (month == 0) {
            sql = "SELECT g.name, COUNT(e.id) as count, COALESCE(SUM(e.amount),0) as total FROM expense_groups g LEFT JOIN expenses e ON g.id = e.group_id AND e.paid_by = ? AND e.expense_year = ? WHERE g.id IN (SELECT group_id FROM group_members WHERE user_id = ?) GROUP BY g.id, g.name ORDER BY total DESC";
        } else {
            sql = "SELECT g.name, COUNT(e.id) as count, COALESCE(SUM(e.amount),0) as total FROM expense_groups g LEFT JOIN expenses e ON g.id = e.group_id AND e.paid_by = ? AND e.expense_year = ? AND e.expense_month = ? WHERE g.id IN (SELECT group_id FROM group_members WHERE user_id = ?) GROUP BY g.id, g.name ORDER BY total DESC";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, year);
            if (month > 0) { ps.setInt(3, month); ps.setInt(4, userId); }
            else { ps.setInt(3, userId); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", rs.getString("name"));
                m.put("count", rs.getInt("count"));
                m.put("total", rs.getDouble("total"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<Map<String, Object>> getDailySummary(int userId, int year, int month) {
        String sql;
        List<Map<String, Object>> list = new ArrayList<>();
        if (month == 0) {
            sql = "SELECT expense_month as month, category, SUM(amount) as total, COUNT(*) as count FROM expenses WHERE paid_by = ? AND expense_year = ? GROUP BY expense_month, category ORDER BY expense_month";
        } else {
            sql = "SELECT DAY(created_at) as day, category, SUM(amount) as total, COUNT(*) as count FROM expenses WHERE paid_by = ? AND expense_year = ? AND expense_month = ? GROUP BY DAY(created_at), category ORDER BY day";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, year);
            if (month > 0) ps.setInt(3, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                if (month == 0) {
                    m.put("month", rs.getInt("month"));
                } else {
                    m.put("day", rs.getInt("day"));
                }
                m.put("category", rs.getString("category"));
                m.put("total", rs.getDouble("total"));
                m.put("count", rs.getInt("count"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static double getUserIncome(int userId) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE paid_by = ? AND category = 'Income'";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static double getUserTotalSpent(int userId) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE paid_by = ? AND category != 'Income'";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static Map<String, Object> getUserProfile(int userId) {
        String sql = "SELECT u.id, u.username, u.email, u.full_name, u.phone, u.balance, COUNT(DISTINCT g.id) as group_count, COUNT(DISTINCT e.id) as expense_count, COALESCE(SUM(e.amount),0) as total_spent FROM users u LEFT JOIN group_members gm ON u.id = gm.user_id LEFT JOIN expense_groups g ON gm.group_id = g.id LEFT JOIN expenses e ON u.id = e.paid_by WHERE u.id = ? GROUP BY u.id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("username", rs.getString("username"));
                m.put("email", rs.getString("email"));
                m.put("fullName", rs.getString("full_name"));
                m.put("phone", rs.getString("phone"));
                m.put("balance", rs.getDouble("balance"));
                m.put("groupCount", rs.getInt("group_count"));
                m.put("expenseCount", rs.getInt("expense_count"));
                m.put("totalSpent", rs.getDouble("total_spent"));
                return m;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean updateUserProfile(int userId, String username, String fullName, String email, String phone) {
        String checkSql = "SELECT id FROM users WHERE (username = ? OR email = ?) AND id != ?";
        try (PreparedStatement ps = getConnection().prepareStatement(checkSql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setInt(3, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return false;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }

        String sql = "UPDATE users SET username = ?, full_name = ?, email = ?, phone = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setInt(5, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean saveOtp(String email, String otpCode, int expirySeconds) {
        String sql = "INSERT INTO otp_codes (email, code, purpose, expires_at) VALUES (?, ?, 'login', DATE_ADD(NOW(), INTERVAL ? SECOND))";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otpCode);
            ps.setInt(3, expirySeconds);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean verifyOtp(String email, String otpCode) {
        String sql = "UPDATE otp_codes SET used = TRUE WHERE email = ? AND code = ? AND used = FALSE AND expires_at > NOW()";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otpCode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean saveToken(int userId, String token, int expiryDays) {
        String sql = "INSERT INTO tokens (user_id, token, expires_at) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? DAY))";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setInt(3, expiryDays);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setBalance(rs.getDouble("balance"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}
