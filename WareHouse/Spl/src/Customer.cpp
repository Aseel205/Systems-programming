
#include "Customer.h"

Customer::Customer(int id, const string &name, int locationDistance, int maxOrders) : status(-1), id(id), name(name), locationDistance(locationDistance), maxOrders(maxOrders), ordersId()

{
}

const string &Customer::getName() const
{

    return name;
}

int Customer::getId() const
{
    return id;
}

int Customer::getCustomerDistance() const
{

    return locationDistance;
}

int Customer::getMaxOrders() const
{

    return maxOrders;
}

int Customer::getNumOrders() const
{ // Returns num of orders the customer has made so far

    return ordersId.size();
}

bool Customer::canMakeOrder() const
{ // Returns true if the customer didn't reach max orders

    return ordersId.size() < static_cast<size_t>(maxOrders);
}

const vector<int> &Customer::getOrdersIds() const
{

    return ordersId;
}

int Customer::addOrder(int orderId)
{ // return OrderId if order was added successfully, -1 otherwise

    if (canMakeOrder())
    {
        ordersId.push_back(orderId);

        return orderId;
    }
    else
        return -1;
}
int Customer::getstatus() const
{
    return status; // we add this
}

SoldierCustomer::SoldierCustomer(int id, const string &name, int locationDistance, int maxOrders) : Customer(id, name, locationDistance, maxOrders)
{
    status = 0;
}

SoldierCustomer *SoldierCustomer::clone() const
{
    return new SoldierCustomer(*this);
}

CivilianCustomer::CivilianCustomer(int id, const string &name, int locationDistance, int maxOrders) : Customer(id, name, locationDistance, maxOrders)
{
    status = 1;
};
CivilianCustomer *CivilianCustomer::clone() const
{
    return new CivilianCustomer(*this);
}
