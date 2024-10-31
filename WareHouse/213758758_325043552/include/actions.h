#pragma once
#include <string>
#include <vector>
#include "WareHouse.h"
class WareHouse;

using std::string;
using std::vector;

enum class ActionStatus
{
    COMPLETED,
    ERROR
};

enum class CustomerType
{
    Soldier,
    Civilian
};

class Customer;

class actions
{
public:
    actions();
    ActionStatus getStatus() const;
    virtual void act(WareHouse &wareHouse) = 0;
    virtual string toString() const = 0;
    virtual actions *clone() const = 0;

    std::string statusToString(ActionStatus status) const; // we added this
    virtual ~actions() = default;                          // we added this

protected:
    void complete();
    void error(string errorMsg);
    string getErrorMsg() const;

private:
    string errorMsg;
    ActionStatus status;
};

class SimulateStep : public actions
{
public:
    SimulateStep(int numOfSteps);
    void act(WareHouse &wareHouse) override;
    std::string toString() const override;
    SimulateStep *clone() const override;

private:
    const int numOfSteps;
};

class AddOrder : public actions
{
public:
    AddOrder(int id);
    void act(WareHouse &wareHouse) override;
    AddOrder *clone() const override;
    string toString() const override;

private:
    const int customerId;
};

class AddCustomer : public actions
{
public:
    AddCustomer(const string &customerName, const string &customerType, int distance, int maxOrders);
    void act(WareHouse &wareHouse) override;
    AddCustomer *clone() const override;
    string toString() const override;

    std::string customerTypetostring(CustomerType customerType) const;      // we added this
    CustomerType stringToCustomerType(const std::string &typeString) const; // we added this

private:
    const string customerName;
    const CustomerType customerType;
    const int distance;
    const int maxOrders;
};

class PrintOrderStatus : public actions
{
public:
    PrintOrderStatus(int id);
    void act(WareHouse &wareHouse) override;
    PrintOrderStatus *clone() const override;
    string toString() const override;

private:
    const int orderId;
};

class PrintCustomerStatus : public actions
{
public:
    PrintCustomerStatus(int customerId);
    void act(WareHouse &wareHouse) override;
    PrintCustomerStatus *clone() const override;
    string toString() const override;

private:
    const int customerId;
};

class PrintVolunteerStatus : public actions
{
public:
    PrintVolunteerStatus(int id);
    void act(WareHouse &wareHouse) override;
    PrintVolunteerStatus *clone() const override;
    string toString() const override;

private:
    const int volunteerId;
};

class PrintActionsLog : public actions
{
public:
    PrintActionsLog();
    void act(WareHouse &wareHouse) override;
    PrintActionsLog *clone() const override;
    string toString() const override;

private:
};

class Close : public actions
{
public:
    Close();
    void act(WareHouse &wareHouse) override;
    string toString() const override;
    Close *clone() const override;

private:
};

class BackupWareHouse : public actions
{
public:
    BackupWareHouse();
    void act(WareHouse &wareHouse) override;
    BackupWareHouse *clone() const override;
    string toString() const override;

private:
};

class RestoreWareHouse : public actions
{
public:
    RestoreWareHouse();
    void act(WareHouse &wareHouse) override;
    RestoreWareHouse *clone() const override;
    string toString() const override;

private:
};
