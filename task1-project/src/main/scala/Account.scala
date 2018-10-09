import exceptions._

class Account(val bank: Bank, initialBalance: Double) {

    class Balance(var amount: Double)

    val balance = new Balance(initialBalance)
    val uid = bank.generateAccountId

    def withdraw(amount: Double): Unit = this.synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot withdraw negative amount")
        else if(amount > getBalanceAmount)
            throw new NoSufficientFundsException("Cannot withdraw amount larger than balance")

        balance.amount -= amount
    }

    def deposit(amount: Double): Unit = this.synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot deposit negative amount")

        balance.amount += amount
    }

    def getBalanceAmount: Double = this.synchronized {
        return balance.amount
    }

    def transferTo(account: Account, amount: Double) = this.synchronized {
        bank.addTransactionToQueue (this, account, amount)
    }


}
