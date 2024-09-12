#include "actions.h"
#include <iostream>

extern WareHouse *backup;

actions::actions() : errorMsg(""), status(ActionStatus::ERROR) {}

ActionStatus actions::getStatus() const
{
    return status;
}

void actions::complete()
{
    status = ActionStatus::COMPLETED;
}

void actions::error(string input)
{
    status = ActionStatus::ERROR;
    errorMsg = input;
    std::cout << errorMsg << std::endl;
}

std::string actions::statusToString(ActionStatus status) const
{
    switch (status)
    {
    case ActionStatus::COMPLETED:
        return "completed";
    case ActionStatus::ERROR:
        return "error";
    }
    return "";
}

string actions::getErrorMsg() const
{
    return errorMsg;
}

void actions::act(WareHouse &wareHouse) {}

// SimulateStep class implementation
SimulateStep::SimulateStep(int numOfSteps) : numOfSteps(numOfSteps) {}

void SimulateStep::act(WareHouse &wareHouse)
{
    for (int i = 0; i < numOfSteps; i++)
        wareHouse.WH_step();

    complete();

    wareHouse.addAction(clone());
}

string SimulateStep::toString() const
{
    return "SimulateStep " + std::to_string(numOfSteps) + " " + statusToString(getStatus());
}

SimulateStep *SimulateStep::clone() const
{
    return new SimulateStep(*this);
}

// AddOrder class implementation
AddOrder::AddOrder(int id) : customerId(id) {}

void AddOrder::act(WareHouse &wareHouse)
{
    bool answer = wareHouse.WH_AddOrder(customerId);
    if (answer)
        complete();
    else
    {
        error("Error: cannot place this order");
    }

    wareHouse.addAction(clone());
}

AddOrder *AddOrder::clone() const
{
    return new AddOrder(*this);
}

string AddOrder::toString() const
{
    return "order " + std::to_string(customerId) + " " + statusToString(getStatus());
}

// AddCustomer class implementation
AddCustomer::AddCustomer(const string &customerName, const string &customerType, int distance, int maxOrders)
    : customerName(customerName), customerType(stringToCustomerType(customerType)), distance(distance), maxOrders(maxOrders)
{
}

CustomerType AddCustomer::stringToCustomerType(const std::string &typeString) const
{
    if (typeString == "soldier")
        return CustomerType::Soldier;
    else if (typeString == "civilian")
        return CustomerType::Civilian;
    else
    {
    }

    return CustomerType::Civilian; // to turn of the warrning
}

std::string AddCustomer::customerTypetostring(CustomerType customerType) const
{
    switch (customerType)
    {
    case CustomerType::Soldier:
        return "soldier";
    case CustomerType::Civilian:
        return "civilian";
    }
    return ""; // to turn of the warrning
}

void AddCustomer::act(WareHouse &wareHouse)
{
    wareHouse.WH_AddCustomer(customerName, customerTypetostring(customerType), distance, maxOrders);
    complete();
    wareHouse.addAction(clone());
}

AddCustomer *AddCustomer::clone() const
{
    return new AddCustomer(*this);
}

string AddCustomer::toString() const
{
    std::string str = "customer " + customerName + " " + customerTypetostring(customerType) + " " + std::to_string(distance) +
                      " " + std::to_string(maxOrders) + " completed";
    return str;
}

// PrintOrderStatus class implementation
PrintOrderStatus::PrintOrderStatus(int id) : orderId(id) {}

void PrintOrderStatus::act(WareHouse &wareHouse)
{
    bool answer = wareHouse.WH_PrintOrderStatus(orderId);

    if (answer)
        complete();
    else
    {
        error("Error: the order doesn't exist");
    }

    wareHouse.addAction(clone());
}

PrintOrderStatus *PrintOrderStatus::clone() const
{
    return new PrintOrderStatus(*this);
}

string PrintOrderStatus::toString() const
{
    return "OrderStatus: " + std::to_string(orderId) + " " + statusToString(getStatus());
}

// PrintCustomerStatus class implementation
PrintCustomerStatus::PrintCustomerStatus(int customerId) : customerId(customerId) {}

void PrintCustomerStatus::act(WareHouse &wareHouse)
{
    bool answer = wareHouse.WH_PrintCustomerStatus(customerId);
    if (answer)
        complete();

    else
    {
        error("Error: Customer doesn't exist");
    }

    wareHouse.addAction(clone());
}

PrintCustomerStatus *PrintCustomerStatus::clone() const
{
    return new PrintCustomerStatus(*this);
}

string PrintCustomerStatus::toString() const
{
    return "CustomerStatus: " + std::to_string(customerId) +
           " " + statusToString(getStatus());
}

// PrintVolunteerStatus class implementation
PrintVolunteerStatus::PrintVolunteerStatus(int id) : volunteerId(id) {}

void PrintVolunteerStatus::act(WareHouse &wareHouse)
{
    bool i = wareHouse.WH_PrintVolunteerStatus(volunteerId);
    if (i)
        complete();
    else
    {
        error("Error: Volunteer doesn't exist !!");
    }

    wareHouse.addAction(clone());
}

PrintVolunteerStatus *PrintVolunteerStatus::clone() const
{
    return new PrintVolunteerStatus(*this);
}

string PrintVolunteerStatus::toString() const
{
    return "VolunteerStatus: " + std::to_string(volunteerId) + " " + statusToString(getStatus());
}

// PrintActionsLog class implementation
PrintActionsLog::PrintActionsLog() {}

void PrintActionsLog::act(WareHouse &wareHouse)
{

    for (actions *action : wareHouse.getActions())
    {
        std::cout << action->toString() << std::endl;
    }

    complete();
    wareHouse.addAction(clone());
}

PrintActionsLog *PrintActionsLog::clone() const
{
    return new PrintActionsLog(*this);
}

string PrintActionsLog::toString() const
{
    return "log: " + statusToString(getStatus());
}

// Close class implementation
Close::Close() {}

void Close::act(WareHouse &wareHouse)
{
    wareHouse.close();
    complete();
}

string Close::toString() const
{
    return "close " + statusToString(getStatus());
}

Close *Close::clone() const
{
    return new Close(*this);
}

// BackupWareHouse class implementation

BackupWareHouse::BackupWareHouse() {}

void BackupWareHouse::act(WareHouse &wareHouse)
{
    if (backup != nullptr)
    {
        delete (backup);
        backup = nullptr;
    }
    WareHouse *temp = (wareHouse.WH_GiveMeCopy());

    backup = temp;

    temp = nullptr;

    complete();
    wareHouse.addAction(clone());
}

BackupWareHouse *BackupWareHouse::clone() const
{
    return new BackupWareHouse(*this);
}

string BackupWareHouse::toString() const
{
    return "Backup " + statusToString(getStatus());
}

// RestoreWareHouse class implementation
RestoreWareHouse::RestoreWareHouse() {}

void RestoreWareHouse::act(WareHouse &wareHouse)
{
    if (backup == nullptr)
    {
        error("Error: No backup available");
        wareHouse.addAction(clone());
    }
    else
    {
        WareHouse temp = ((*backup));
        wareHouse = temp;
        complete();
        wareHouse.addAction(clone());
    }
}

RestoreWareHouse *RestoreWareHouse::clone() const
{
    return new RestoreWareHouse(*this);
}

string RestoreWareHouse::toString() const
{
    return "restore " + statusToString(getStatus());
}
