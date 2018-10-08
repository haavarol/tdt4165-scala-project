import exceptions._

class Account(val bank: Bank, initialBalance: Double) {

    class Balance(var amount: Double)

    val balance = new Balance(initialBalance)
    val uid = bank.generateAccountId

    def withdraw(amount: Double): Unit = synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot withdraw negativ amount")
        else if(amount > getBalanceAmount)
            throw new NoSufficientFundsException("Cannot withdraw amount larger than balance")

        balance.amount -= amount
    }

    def deposit(amount: Double): Unit = synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot deposit negativ amount")

        balance.amount += amount
    }

    def getBalanceAmount: Double = synchronized {
        return balance.amount
    }

    def transferTo(account: Account, amount: Double) = synchronized {
        bank.addTransactionToQueue (this, account, amount)
    }


}
