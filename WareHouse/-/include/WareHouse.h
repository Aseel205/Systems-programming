#ifndef WAREHOUSE_H
#define WAREHOUSE_H
#include <string>
#include <vector>
using namespace std;
#include "Customer.h"
#include <iostream>
#include <stdbool.h> // for boolean
#include "Volunteer.h"
#include "actions.h"
#include "Order.h"
class actions;
class Volunteer;
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <algorithm>
// Warehouse responsible for Volunteers, Customers and Actions.

class WareHouse
{

public:
    WareHouse(const string &configFilePath);
    void start();
    void addOrder(Order *order);
    void addAction(actions *action);
    Customer &getCustomer(int customerId) const;
    Volunteer &getVolunteer(int volunteerId) const;
    Order &getOrder(int orderId) const;
    const vector<actions *> &getActions() const;
    void close();
    void open();

    // we add this
    int create_customer_id();
    int create_order_id();
    int create_volunteer_id();
    void addcustomer_warehouse(Customer *customer);
    int get_customer_counter();
    int get_order_counter();
    bool WH_AddOrder(int customerId) const;
    bool WH_AddCustomer(string customerName, string customerType, int distance, int maxOrders);
    bool WH_PrintOrderStatus(int orderid);
    bool WH_PrintCustomerStatus(int customerId);
    bool WH_PrintVolunteerStatus(int VolunteerId);
    void WH_step();
    WareHouse &operator=(const WareHouse &ourhouse);
    ~WareHouse();
    void add_collector(std::string name, int cooldown);
    void add_limited_collector(std::string name, int cooldown, int maxOrders);
    void add_driver(std::string name, int maxdistance, int distancePerStep);
    void add_limited_driver(std::string name, int maxdistance, int distancePerStep, int maxOrders);
    void add_soldier_customer(std::string name, int distance, int maxOrders);
    void add_civilian_customer(std::string name, int distance, int maxOrders);
    void readConfigFile(const std::string &filePath);
    bool WH_AddOrder(int customerId);
    WareHouse(const WareHouse &other);
    void delete_volunteer(int id);
    const vector<Volunteer *> &getvolunteers() const;
    WareHouse *WH_GiveMeCopy() const;
    WareHouse(WareHouse &&other) noexcept;
    WareHouse &operator=(WareHouse &&other) noexcept;

private:
    bool isOpen;
    vector<actions *> actionsLog;
    vector<Volunteer *> volunteers;
    vector<Order *> pendingOrders;
    vector<Order *> inProcessOrders;
    vector<Order *> completedOrders;
    vector<Customer *> customers;
    int customerCounter;  // For assigning unique customer IDs
    int volunteerCounter; // For assigning unique volunteer IDs
    int ordercounter;     // we add this
};
#endif // WAREHOUSE_H